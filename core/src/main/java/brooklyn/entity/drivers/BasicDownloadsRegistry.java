package brooklyn.entity.drivers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import brooklyn.config.StringConfigMap;
import brooklyn.util.text.Strings;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BasicDownloadsRegistry implements DownloadResolverRegistry {

    private final List<Function<? super DownloadRequirement, ? extends DownloadTargets>> producers = Lists.newCopyOnWriteArrayList();

    private final List<Function<? super DownloadRequirement, String>> filenameProducers = Lists.newCopyOnWriteArrayList();

    /**
     * The default is (in-order) to:
     * <ol>
     *   <li>Use the local repo, if any (defaulting to $HOME/.brooklyn/repository)
     *   <li>Use brooklyn properties for any download overrides defined there (see {@link DownloadPropertiesResolver}
     *   <li>Use the entity's Attributes.DOWNLOAD_URL
     *   <li>Use the cloudsoft fallback repo
     * </ol>
     * @param config
     * @return
     */
    public static BasicDownloadsRegistry newDefault(StringConfigMap config) {
        BasicDownloadsRegistry result = new BasicDownloadsRegistry();
        
        // In-order, will look up: local repo, overrides defined in the properties, and then 
        // the entity's attribute to get the download URL
        DownloadLocalRepoResolver localRepoProducer = new DownloadLocalRepoResolver(config);
        DownloadPropertiesResolver propertiesProducer = new DownloadPropertiesResolver(config);
        DownloadUrlAttributeProducer attributeProducer = new DownloadUrlAttributeProducer();
        DownloadCloudsoftRepoResolver cloudsoftRepoProducer = new DownloadCloudsoftRepoResolver(config);
        
        result.registerProducer(localRepoProducer);
        result.registerProducer(propertiesProducer);
        result.registerProducer(attributeProducer);
        result.registerProducer(cloudsoftRepoProducer);
        
        result.registerFilenameProducer(FilenameProducers.fromFilenameProperty());
        result.registerFilenameProducer(FilenameProducers.firstPrimaryTargetOf(propertiesProducer));
        result.registerFilenameProducer(FilenameProducers.firstPrimaryTargetOf(attributeProducer));
        
        return result;
    }
    
    public static BasicDownloadsRegistry newEmpty() {
        return new BasicDownloadsRegistry();
    }
    
    @Override
    public void registerPrimaryProducer(Function<? super DownloadRequirement, ? extends DownloadTargets> producer) {
        producers.add(0, checkNotNull(producer, "resolver"));
    }

    @Override
    public void registerProducer(Function<? super DownloadRequirement, ? extends DownloadTargets> producer) {
        producers.add(checkNotNull(producer, "resolver"));
    }

    @Override
    public void registerFilenameProducer(Function<? super DownloadRequirement, String> producer) {
        filenameProducers.add(checkNotNull(producer, "producer"));
    }

    @Override
    public DownloadResolver resolve(EntityDriver driver) {
        return resolve(new BasicDownloadRequirement(driver));
    }

    @Override
    public DownloadResolver resolve(EntityDriver driver, Map<String, ?> properties) {
        return resolve(new BasicDownloadRequirement(driver, properties));
    }

    @Override
    public DownloadResolver resolve(EntityDriver driver, String addonName, Map<String, ?> addonProperties) {
        return resolve(new BasicDownloadRequirement(driver, addonName, addonProperties));
    }

    private DownloadResolver resolve(DownloadRequirement req) {
        // Infer filename
        String filename = null;
        for (Function<? super DownloadRequirement, String> filenameProducer : filenameProducers) {
            filename = filenameProducer.apply(req);
            if (!Strings.isBlank(filename)) break;
        }
        
        // If a filename-producer has given us the filename, then augment the DownloadRequirement with that
        // (so that local-repo substitutions etc can use that explicit filename)
        DownloadRequirement wrappedReq;
        if (filename == null) {
            wrappedReq = req;
        } else {
            wrappedReq = BasicDownloadRequirement.copy(req, ImmutableMap.of("filename", filename));
        }
        
        // Get ordered download targets to be tried
        List<String> primaries = Lists.newArrayList();
        List<String> fallbacks = Lists.newArrayList();
        for (Function<? super DownloadRequirement, ? extends DownloadTargets> producer : producers) {
            DownloadTargets vals = producer.apply(wrappedReq);
            primaries.addAll(vals.getPrimaryLocations());
            fallbacks.addAll(vals.getFallbackLocations());
            if (!vals.canContinueResolving()) {
                break;
            }
        }

        Set<String> result = Sets.newLinkedHashSet();
        result.addAll(primaries);
        result.addAll(fallbacks);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No downloads matched for "+req);
        }
        
        // If filename-producers didn't give any explicit filename, then infer from download results
        if (filename == null) {
            for (String target : result) {
                filename = FilenameProducers.inferFilename(target);
                if (!Strings.isBlank(filename)) break;
            }
        }
        if (Strings.isBlank(filename)) {
            throw new IllegalArgumentException("No filenames matched for "+req+" (targets "+result+")");
        }
        
        // And return the result
        return new BasicDownloadResolver(result, filename);
    }
}
