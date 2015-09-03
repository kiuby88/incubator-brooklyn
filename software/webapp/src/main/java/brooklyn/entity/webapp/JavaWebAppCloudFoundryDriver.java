/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package brooklyn.entity.webapp;

import brooklyn.entity.basic.EntityLocal;
import brooklyn.entity.java.JavaSoftwareProcessDriver;
import brooklyn.location.cloudfoundry.CloudFoundryPaasLocation;
import org.cloudfoundry.client.lib.domain.Staging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JavaWebAppCloudFoundryDriver extends AbstractApplicationCloudFoundryDriver
implements JavaSoftwareProcessDriver, JavaWebAppDriver{

    private String applicationName;
    private String applicationWarUrl;

    public JavaWebAppCloudFoundryDriver(EntityLocal entity, CloudFoundryPaasLocation location) {
        super(entity, location);
    }

    public JavaWebAppSoftwareProcessImpl getEntity() {
        return (JavaWebAppSoftwareProcessImpl) super.getEntity();
    }

    @Override
    protected void init() {
        super.init();
        initApplicationParameters();
    }

    @SuppressWarnings("unchecked")
    private void initApplicationParameters() {
        //TODO: Probably, this method could be moved to the super class.
        //but, a new configkey should be necessary to specify the deployment
        //artifact (war) without using the java service.
        applicationWarUrl = getEntity().getConfig(JavaWebAppService.ROOT_WAR);

        String nameWithExtension=getFilenameContextMapper()
                .findArchiveNameFromUrl(applicationWarUrl, true);
        applicationName  = nameWithExtension.substring(0, nameWithExtension.indexOf('.'));

        //These values shouldn't be null or empty
        checkNotNull(applicationWarUrl, "application war url");
        checkNotNull(applicationName, "application name");
    }

    @Override
    public String getBuildpack(){
        return getEntity().getBuildpack();
    }

    protected String getApplicationUrl(){
        return applicationWarUrl;
    }

    protected String getApplicationName(){
        return applicationName;
    }

    @Override
    public void deploy() {
        getEntity().deploy(applicationWarUrl, applicationName);
    }

    @Override
    public Set<String> getEnabledProtocols() {
        return null;
    }

    @Override
    public Integer getHttpPort() {
        return null;
    }

    @Override
    public Integer getHttpsPort() {
        return null;
    }

    @Override
    public HttpsSslConfig getHttpsSslConfig() {
        return null;
    }

    @Override
    public void deploy(File file) {
        deploy(file, null);
    }

    @Override
    public void deploy(File f, String targetName) {
        if (targetName == null) {
            targetName = f.getName();
        }
        deploy(f.toURI().toASCIIString(), targetName);
    }

    @Override
    public String deploy(String url, String targetName) {
        List<String> uris = new ArrayList<String>();
        Staging staging;
        File war;

        try {
            staging = new Staging(null, getBuildpack());
            uris.add(inferApplicationDomainUri(getApplicationName()));

            war=LocalResourcesDownloader
                    .downloadResourceInLocalDir(getApplicationUrl());

            getClient().createApplication(getApplicationName(), staging,
                    getLocation().getConfig(CloudFoundryPaasLocation.REQUIRED_MEMORY),
                    uris, null);
            getClient().uploadApplication(getApplicationName(), war.getCanonicalPath());
        } catch (IOException e) {
            log.error("Error deploying application {} managed by driver {}",
                    new Object[]{getEntity(), this});
        }

        return targetName;
    }

    @Override
    public void undeploy(String targetName) {

    }

    @Override
    public FilenameToWebContextMapper getFilenameContextMapper() {
        return new FilenameToWebContextMapper();
    }

    @Override
    public boolean isJmxEnabled() {
        return false;
    }


}
