package brooklyn.entity.webapp;

import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.location.access.BrooklynAccessUtils;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;


public abstract class PhpWebAppSoftwareProcessImpl extends SoftwareProcessImpl implements PhpWebAppSoftwareProcess{
    private static final Logger LOG= LoggerFactory.getLogger(PhpWebAppSoftwareProcessImpl.class);

    public PhpWebAppSoftwareProcessImpl(){ super(); }

    public PhpWebAppSoftwareProcessImpl(Entity parent){
        this(new LinkedHashMap(),parent);
    }

    public PhpWebAppSoftwareProcessImpl(Map flags){
        this(flags, null);
    }

    public PhpWebAppSoftwareProcessImpl(Map flags, Entity parent) {
        super(flags, parent);
    }

    public PhpWebAppDriver getDriver(){
        return (PhpWebAppDriver) super.getDriver();
    }

    protected Set<String> getEnabledProtocols() {
        return getAttribute(PhpWebAppSoftwareProcess.ENABLED_PROTOCOLS);
    }

    protected Set<String> getDeployedPhpAppsAttribute() {
        return getAttribute(DEPLOYED_PHP_APPS);
    }

    protected int getHttpPort(){ return getAttribute(HTTP_PORT);}

    protected void setDeployedPhpAppsAttribute(Set<String> deployedPhpApps){
        setAttribute(DEPLOYED_PHP_APPS, deployedPhpApps);
    }

    @Override
    protected void connectSensors(){
        super.connectSensors();
        WebAppServiceMethods.connectWebAppServerPolicies(this);
    }

    @Override
    protected void doStop(){
        super.doStop();
        //zero our workrate derived workrates.
        //TODO might not be enough, as a policy may still be executing and have a record of historic vals;
        // should remove policies
        //also nor sure we want this; implies more generally a resposibility for sensor to announce things
        //disconnected
        putEnricherValuesToNullValue();
    }

    private void putEnricherValuesToNullValue(){
        setAttribute(REQUESTS_PER_SECOND_LAST, 0D);
        setAttribute(REQUESTS_PER_SECOND_IN_WINDOW, 0D);
    }

    // TODO thread-safety issues: if multiple concurrent calls, may break (e.g. deployment_wars being reset)
    public void deployInitialApplications() {
        initDeployAppAttributeIfIsNull();
        deployInitialAppGitSource();
        deployInitialAppTarballResource();
    }

    private void initDeployAppAttributeIfIsNull(){
        if (getDeployedPhpAppsAttribute() == null)
            setDeployedPhpAppsAttribute(Sets.<String>newLinkedHashSet());
    }

    private void deployInitialAppGitSource() {
        String gitRepoUrl = getConfig(APP_GIT_REPO_URL);
        if (gitRepoUrl!=null){
            String targetName=inferCorrectAppGitName();
            deployGitResource(gitRepoUrl, targetName);
        }
    }

    private String inferCorrectAppGitName(){
        String result;
        if(getConfig(APP_NAME)==null || getConfig(APP_NAME).equals("")){
            result=getDriver().getSourceNameResolver().getNameOfRepositoryGitFromHttpsUrl(getConfig(APP_GIT_REPO_URL));
        }
        else{
            result=getConfig(APP_NAME);
        }
        return result;
    }

    private void deployInitialAppTarballResource(){
        String tarballResourceUrl = getConfig(APP_TARBALL_URL);
        if (tarballResourceUrl!=null){
            String targetName=inferCorrectAppTarballName();
            deployTarballResource(tarballResourceUrl, targetName);
        }
    }

    private String  inferCorrectAppTarballName(){
        String result;
        if(getConfig(APP_NAME)==null || getConfig(APP_NAME).equals("")){
            result=getDriver().getSourceNameResolver().getIdOfTarballFromUrl(getConfig(APP_TARBALL_URL));
        }
        else{
            result=getConfig(APP_NAME);
        }
        return result;
    }

    @Effector(description="Deploys the given artifact, from a source URL, to a given deployment filename/context")
    public void deployGitResource(
            @EffectorParam(name="url", description="URL of git Repo file") String url,
            @EffectorParam(name="targetName", description="Application Name") String targetName) {
        try {
            deployGitPhpApp(url, targetName);
        } catch (RuntimeException e) {
            LOG.warn("Error deploying '"+url+"' on "+toString()+"; rethrowing...", e);
            throw Throwables.propagate(e);
        }
    }

    private void deployGitPhpApp(String url, String targetName){
        checkNotNull(url, "url");
        PhpWebAppDriver driver =   getDriver();
        String deployedAppName = driver.deployGitResource(url, targetName);
        updateDeploymentSensorToDeployAnApp(deployedAppName);
    }

    private void updateDeploymentSensorToDeployAnApp(String deployedAppName){
        Set<String> deployedPhpApps = getDeployedPhpAppsAttribute();
        if (deployedPhpApps == null) {
            deployedPhpApps = Sets.newLinkedHashSet();
        }
        deployedPhpApps.add(deployedAppName);
        setDeployedPhpAppsAttribute(deployedPhpApps);
    }

    @Effector(description="Deploys the given artifact, from a source URL, to a given deployment filename/context")
    public void deployTarballResource(
            @EffectorParam(name="url", description="URL of tarball resource") String url,
            @EffectorParam(name="targetName", description="Application Name") String targetName){
        try {
            deployTarballPhpApp(url, targetName);
        } catch (RuntimeException e) {
            LOG.warn("Error deploying '"+url+"' on "+toString()+"; rethrowing...", e);
            throw Throwables.propagate(e);
        }
    }

    private void deployTarballPhpApp(String url, String targetName){
        //TODO deployment git resource
        checkNotNull(url, "url");
        PhpWebAppDriver driver =   getDriver();
        String deployedAppName = driver.deployTarballResource(url, targetName);
        updateDeploymentSensorToDeployAnApp(deployedAppName);
    }

    /** For the DEPLOYED_PHP_APP to be updated, the input must match the result of the call to deploy */
    @Override
    @Effector(description="Undeploys the given context/artifact")
    public void undeploy(
            @EffectorParam(name="deployedAppName") String targetName) {
        try {

            undeployPhpApp(targetName);

        } catch (RuntimeException e) {
            LOG.warn("Error undeploying '"+targetName+"' on "+toString()+"; rethrowing...", e);
            throw Throwables.propagate(e);
        }
    }
    private void undeployPhpApp(String targetName){
        PhpWebAppDriver driver = getDriver();
        driver.undeploy(targetName);
        updateDeploymentSensorToUndeployAnApp(targetName);
    }

    private void updateDeploymentSensorToUndeployAnApp(String targetName){
        initDeployAppAttributeIfIsNull();
        Set<String> deployedPhpApps=getDeployedPhpAppsAttribute();
        deployedPhpApps.remove(targetName);
        setDeployedPhpAppsAttribute(deployedPhpApps);
    }

}
