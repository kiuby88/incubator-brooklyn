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
package brooklyn.entity.basic;

import brooklyn.entity.software.lifecycle.AbstractLifecycleEffectorTasks;
import brooklyn.location.Location;
import brooklyn.location.paas.PaasLocation;
import brooklyn.util.config.ConfigBag;
import brooklyn.util.exceptions.Exceptions;
import com.google.common.annotations.Beta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Beta
public class PaasLifecycleEffectorTasks extends AbstractLifecycleEffectorTasks {

    private static final Logger log = LoggerFactory.getLogger(PaasLifecycleEffectorTasks.class);

    @Override
    protected SoftwareProcessImpl entity() {
        return (SoftwareProcessImpl) super.entity();
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        ServiceStateLogic.setExpectedState(entity(), Lifecycle.STARTING);
        try {

            PaasLocation location = (PaasLocation)entity().getLocation(locations);

            preStartProcess(location);
            startProcess();
            postStartProcess();

            ServiceStateLogic.setExpectedState(entity(), Lifecycle.RUNNING);
        } catch (Throwable t) {
            ServiceStateLogic.setExpectedState(entity(), Lifecycle.ON_FIRE);
            log.error("Error error starting entity {}", entity());
            throw Exceptions.propagate(t);
        }
    }

    //TODO: This method could be uploaded to the super class.
    protected void preStartProcess(PaasLocation location){
        createDriver(location);
        entity().preStart();
    }

    /**
     * Create the driver ensuring that the location is ready.
     */
    private void createDriver(PaasLocation location) {
        if (location != null) {
            entity().initDriver(location);
        } else {
            throw new ExceptionInInitializerError("Location should not be null in " + this +
                    " the driver needs a initialized Location");
        }
    }

    protected void startProcess(){
        entity().getDriver().start();
    }

    protected void postStartProcess(){
        entity().postDriverStart();
        if(entity().connectedSensors){
            log.debug("skipping connecting sensors for "+entity()+" " +
                    "in driver-tasks postStartCustom because already connected (e.g. restarting)");
        } else {
            log.debug("connecting sensors for "+entity()+" in driver-tasks postStartCustom because already connected (e.g. restarting)");
            entity().connectSensors();
        }
        entity().waitForServiceUp();
        entity().postStart();

    }

    /**
     * Default restart implementation for an entity.
     * <p>
     * Stops processes if possible, then starts the entity again.
     */
    @Override
    public void restart(ConfigBag parameters) {
        //TODO
    }

    @Override
    public void stop(ConfigBag parameters) {
        //TODO
    }

}