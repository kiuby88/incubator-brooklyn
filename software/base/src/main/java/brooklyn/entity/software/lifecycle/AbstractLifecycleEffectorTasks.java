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
package brooklyn.entity.software.lifecycle;


import brooklyn.config.ConfigKey;
import brooklyn.entity.Effector;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.BrooklynTaskTags;
import brooklyn.entity.basic.EffectorStartableImpl;
import brooklyn.entity.basic.EntityInternal;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.effector.EffectorBody;
import brooklyn.entity.effector.Effectors;
import brooklyn.entity.trait.Startable;
import brooklyn.location.Location;
import brooklyn.location.MachineLocation;
import brooklyn.location.MachineProvisioningLocation;
import brooklyn.location.basic.Locations;
import brooklyn.util.config.ConfigBag;
import brooklyn.util.guava.Maybe;
import brooklyn.util.task.Tasks;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractLifecycleEffectorTasks implements LifecycleEffectorTasks{


    private static final Logger log = LoggerFactory.getLogger(AbstractLifecycleEffectorTasks.class);
    public static final ConfigKey<Collection<? extends Location>> LOCATIONS = EffectorStartableImpl.StartParameters.LOCATIONS;


    /** Attaches lifecycle effectors (start, restart, stop) to the given entity post-creation. */
    public void attachLifecycleEffectors(Entity entity) {
        ((EntityInternal) entity).getMutableEntityType().addEffector(newStartEffector());
        ((EntityInternal) entity).getMutableEntityType().addEffector(newRestartEffector());
        ((EntityInternal) entity).getMutableEntityType().addEffector(newStopEffector());
    }

    /**
     * Return an effector suitable for setting in a {@code public static final} or attaching dynamically.
     * <p>
     * The effector overrides the corresponding effector from {@link brooklyn.entity.trait.Startable} with
     * the behaviour in this lifecycle class instance.
     */
    public Effector<Void> newStartEffector() {
        return Effectors.effector(Startable.START).impl(newStartEffectorTask()).build();
    }

    /** @see {@link #newStartEffector()} */
    public Effector<Void> newRestartEffector() {
        return Effectors.effector(Startable.RESTART)
                .parameter(SoftwareProcess.RestartSoftwareParameters.RESTART_CHILDREN)
                .parameter(SoftwareProcess.RestartSoftwareParameters.RESTART_MACHINE)
                .impl(newRestartEffectorTask())
                .build();
    }

    /** @see {@link #newStartEffector()} */
    public Effector<Void> newStopEffector() {
        return Effectors.effector(Startable.STOP)
                .parameter(SoftwareProcess.StopSoftwareParameters.STOP_PROCESS_MODE)
                .parameter(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE)
                .impl(newStopEffectorTask())
                .build();
    }

    /**
     * Returns the {@link brooklyn.management.TaskFactory} which supplies the implementation for the start effector.
     * <p>
     * Calls {@link #start(java.util.Collection)} in this class.
     */
    public EffectorBody<Void> newStartEffectorTask() {
        return new EffectorBody<Void>() {
            @Override
            public Void call(ConfigBag parameters) {
                Collection<? extends Location> locations  = null;

                Object locationsRaw = parameters.getStringKey(LOCATIONS.getName());
                locations = Locations.coerceToCollection(entity().getManagementContext(), locationsRaw);

                if (locations==null) {
                    // null/empty will mean to inherit from parent
                    locations = Collections.emptyList();
                }

                start(locations);
                return null;
            }
        };
    }

    /**
     * Calls {@link #restart(ConfigBag)}.
     *
     * @see {@link #newStartEffectorTask()}
     */
    public EffectorBody<Void> newRestartEffectorTask() {
        return new EffectorBody<Void>() {
            @Override
            public Void call(ConfigBag parameters) {
                restart(parameters);
                return null;
            }
        };
    }

    /**
     * Calls {@link #stop()}.
     *
     * @see {@link #newStartEffectorTask()}
     */
    public EffectorBody<Void> newStopEffectorTask() {
        return new EffectorBody<Void>() {
            @Override
            public Void call(ConfigBag parameters) {
                stop(parameters);
                return null;
            }
        };
    }

    protected EntityInternal entity() {
        return (EntityInternal) BrooklynTaskTags.getTargetOrContextEntity(Tasks.current());
    }

    protected Location getLocation(@Nullable Collection<? extends Location> locations) {
        if (locations==null || locations.isEmpty()) locations = entity().getLocations();
        if (locations.isEmpty()) {
            MachineProvisioningLocation<?> provisioner = (MachineProvisioningLocation) entity()
                    .getAttribute(SoftwareProcess.PROVISIONING_LOCATION);
            if (provisioner!=null) locations = Arrays.<Location>asList(provisioner);
        }
        locations = Locations.getLocationsCheckingAncestors(locations, entity());

        Maybe<MachineLocation> ml = Locations.findUniqueMachineLocation(locations);
        if (ml.isPresent()) return ml.get();

        if (locations.isEmpty())
            throw new IllegalArgumentException("No locations specified when starting "+entity());
        if (locations.size() != 1 || Iterables.getOnlyElement(locations)==null)
            throw new IllegalArgumentException("Ambiguous locations detected when starting "+entity()+": "+locations);
        return Iterables.getOnlyElement(locations);
    }

    /** @deprecated since 0.7.0 use {@link #restart(ConfigBag)} */
    @Deprecated
    public void restart() {
        restart(ConfigBag.EMPTY);
    }

    /** @deprecated since 0.7.0 use {@link #stop(ConfigBag)} */
    @Deprecated
    public void stop() {
        stop(ConfigBag.EMPTY);
    }

    @Override
    public abstract void start(Collection<? extends Location> locations);

    @Override
    public abstract void restart(ConfigBag parameters);

    @Override
    public abstract void stop(ConfigBag parameters);

}
