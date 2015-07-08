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
package brooklyn.entity.basic.behaviour.softwareprocesss.flagssupplier;


import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractEntity;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.location.Location;
import brooklyn.location.MachineProvisioningLocation;
import brooklyn.location.PortRange;
import brooklyn.location.basic.LocationConfigKeys;
import brooklyn.location.cloud.CloudLocationConfig;
import brooklyn.util.collections.MutableSet;
import brooklyn.util.config.ConfigBag;
import brooklyn.util.flags.TypeCoercions;
import brooklyn.util.guava.Maybe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MachineProvisioningLocationFlagsSupplier extends AbstractLocationFlagsSupplier {

    private static final Logger log = LoggerFactory.getLogger(MachineProvisioningLocationFlagsSupplier.class);


    public MachineProvisioningLocationFlagsSupplier(AbstractEntity entity) {
        super(entity);
    }

    @Override
    public Map<String,Object> obtainFlagsForLocation(Location location){
        return obtainProvisioningFlags((MachineProvisioningLocation)location);
    }

    protected Map<String,Object> obtainProvisioningFlags(MachineProvisioningLocation location) {
        ConfigBag result = ConfigBag.newInstance(location.getProvisioningFlags(ImmutableList.of(getClass().getName())));
        result.putAll(entity().getConfig(SoftwareProcessImpl.PROVISIONING_PROPERTIES));
        if (result.get(CloudLocationConfig.INBOUND_PORTS) == null) {
            Collection<Integer> ports = getRequiredOpenPorts();
            Object requiredPorts = result.get(CloudLocationConfig.ADDITIONAL_INBOUND_PORTS);
            if (requiredPorts instanceof Integer) {
                ports.add((Integer) requiredPorts);
            } else if (requiredPorts instanceof Iterable) {
                for (Object o : (Iterable<?>) requiredPorts) {
                    if (o instanceof Integer) ports.add((Integer) o);
                }
            }
            if (ports != null && ports.size() > 0) result.put(CloudLocationConfig.INBOUND_PORTS, ports);
        }
        result.put(LocationConfigKeys.CALLER_CONTEXT, this);
        return result.getAllConfigMutable();
    }

    /** returns the ports that this entity wants to use;
     * default implementation returns {@link brooklyn.entity.basic.SoftwareProcess#REQUIRED_OPEN_LOGIN_PORTS} plus first value
     * for each {@link PortAttributeSensorAndConfigKey} config key {@link brooklyn.location.PortRange}
     * plus any ports defined with a config keys ending in {@code .port}.
     */
    public Collection<Integer> getRequiredOpenPorts() {
        Set<Integer> ports = MutableSet
                .copyOf(entity().getConfig(SoftwareProcessImpl.REQUIRED_OPEN_LOGIN_PORTS));
        Map<ConfigKey<?>, ?> allConfig = entity().config().getBag().getAllConfigAsConfigKeyMap();
        Set<ConfigKey<?>> configKeys = Sets.newHashSet(allConfig.keySet());
        configKeys.addAll(entity().getEntityType().getConfigKeys());

        /* TODO: This won't work if there's a port collision, which will cause the corresponding port attribute
           to be incremented until a free port is found. In that case the entity will use the free port, but the
           firewall will open the initial port instead. Mostly a problem for SameServerEntity, localhost location.
         */
        for (ConfigKey<?> k: configKeys) {
            Object value;
            if (PortRange.class.isAssignableFrom(k.getType()) || k.getName().matches(".*\\.port")) {
                value = entity().config().get(k);
            } else {
                // config().get() will cause this to block until all config has been resolved
                // using config().getRaw(k) means that we won't be able to use e.g. 'http.port: $brooklyn:component("x").attributeWhenReady("foo")'
                // but that's unlikely to be used
                Maybe<Object> maybeValue = entity().config().getRaw(k);
                value = maybeValue.isPresent() ? maybeValue.get() : null;
            }

            Maybe<PortRange> maybePortRange = TypeCoercions.tryCoerce(value, new TypeToken<PortRange>() {
            });

            if (maybePortRange.isPresentAndNonNull()) {
                PortRange p = maybePortRange.get();
                if (p != null && !p.isEmpty()) ports.add(p.iterator().next());
            }
        }

        log.debug("getRequiredOpenPorts detected default {} for {}", ports, entity());
        return ports;
    }

}
