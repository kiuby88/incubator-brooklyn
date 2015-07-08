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

import brooklyn.entity.Entity;
import brooklyn.entity.software.lifecycle.AbstractLifecycleEffectorTasks;
import brooklyn.entity.software.lifecycle.LifecycleEffectorTasks;
import brooklyn.location.Location;
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
    public void attachLifecycleEffectors(Entity entity) {

    }

    @Override
    public void start(Collection<? extends Location> locations) {
        ServiceStateLogic.setExpectedState(entity(), Lifecycle.STARTING);
        try {






            ServiceStateLogic.setExpectedState(entity(), Lifecycle.RUNNING);
        } catch (Throwable t) {
            ServiceStateLogic.setExpectedState(entity(), Lifecycle.ON_FIRE);
            log.error("Error error starting entity {}", entity());
            throw Exceptions.propagate(t);
        }

    }

    private void preStart() {
        //createDriver();
    }


    @Override
    public void restart(ConfigBag parameters) {

    }

    @Override
    public void stop(ConfigBag parameters) {

    }

//    private static final Logger log = LoggerFactory.getLogger(PaasLifecycleEffectorTasks.class);
//
//    public static final ConfigKey<Collection<? extends Location>> LOCATIONS = EffectorStartableImpl.StartParameters.LOCATIONS;
//    public static final ConfigKey<Duration> STOP_PROCESS_TIMEOUT = ConfigKeys.newConfigKey(Duration.class,
//            "process.stop.timeout", "How long to wait for the processes to be stopped; use null to mean forever", Duration.TWO_MINUTES);
//
//    /** Attaches lifecycle effectors (start, restart, stop) to the given entity post-creation. */
//    public void attachLifecycleEffectors(Entity entity) {
//        este metodo debe de ser cambiado
//
//        ((EntityInternal) entity).getMutableEntityType().addEffector(newStartEffector());
//        ((EntityInternal) entity).getMutableEntityType().addEffector(newRestartEffector());
//        ((EntityInternal) entity).getMutableEntityType().addEffector(newStopEffector());
//    }
//
//    /**
//     * Return an effector suitable for setting in a {@code public static final} or attaching dynamically.
//     * <p>
//     * The effector overrides the corresponding effector from {@link brooklyn.entity.trait.Startable} with
//     * the behaviour in this lifecycle class instance.
//     */
//    public Effector<Void> newStartEffector() {
//        return Effectors.effector(Startable.START).impl(newStartEffectorTask()).build();
//    }
//
//    /** @see {@link #newStartEffector()} */
//    public Effector<Void> newRestartEffector() {
//        review this code
//        return Effectors.effector(Startable.RESTART)
//                .parameter(SoftwareProcess.RestartSoftwareParameters.RESTART_CHILDREN)
//                .parameter(SoftwareProcess.RestartSoftwareParameters.RESTART_MACHINE)
//                .impl(newRestartEffectorTask())
//                .build();
//    }
//
//    /** @see {@link #newStartEffector()} */
//    public Effector<Void> newStopEffector() {
//        review this code
//        return Effectors.effector(Startable.STOP)
//                .parameter(SoftwareProcess.StopSoftwareParameters.STOP_PROCESS_MODE)
//                .parameter(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE)
//                .impl(newStopEffectorTask())
//                .build();
//    }
//
//    /**
//     * Returns the {@link brooklyn.management.TaskFactory} which supplies the implementation for the start effector.
//     * <p>
//     * Calls {@link #start(Collection)} in this class.
//     */
//    public EffectorBody<Void> newStartEffectorTask() {
//        return new EffectorBody<Void>() {
//            @Override
//            public Void call(ConfigBag parameters) {
//                Collection<? extends Location> locations  = null;
//
//                Object locationsRaw = parameters.getStringKey(LOCATIONS.getName());
//                locations = Locations.coerceToCollection(entity().getManagementContext(), locationsRaw);
//
//                if (locations==null) {
//                    // null/empty will mean to inherit from parent
//                    locations = Collections.emptyList();
//                }
//
//                start(locations);
//                return null;
//            }
//        };
//    }
//
//    /**
//     * Calls {@link #restart(ConfigBag)}.
//     *
//     * @see {@link #newStartEffectorTask()}
//     */
//    public EffectorBody<Void> newRestartEffectorTask() {
//        return new EffectorBody<Void>() {
//            @Override
//            public Void call(ConfigBag parameters) {
//                restart(parameters);
//                return null;
//            }
//        };
//    }
//
//    /**
//     * Calls {@link #stop()}.
//     *
//     * @see {@link #newStartEffectorTask()}
//     */
//    public EffectorBody<Void> newStopEffectorTask() {
//        return new EffectorBody<Void>() {
//            @Override
//            public Void call(ConfigBag parameters) {
//                stop(parameters);
//                return null;
//            }
//        };
//    }
//
//    @Override
//    protected CloudFoundryEntityImpl entity() {
//        return (CloudFoundryEntityImpl) entity();
//    }
//
//    protected Location getLocation(@Nullable Collection<? extends Location> locations) {
//
//        if (locations==null || locations.isEmpty()) locations = entity().getLocations();
//        if (locations.isEmpty()) {
//            MachineProvisioningLocation<?> provisioner = entity().getAttribute(SoftwareProcess.PROVISIONING_LOCATION);
//            if (provisioner!=null) locations = Arrays.<Location>asList(provisioner);
//        }
//        locations = Locations.getLocationsCheckingAncestors(locations, entity());
//
//        Maybe<MachineLocation> ml = Locations.findUniqueMachineLocation(locations);
//        if (ml.isPresent()) return ml.get();
//
//        if (locations.isEmpty())
//            throw new IllegalArgumentException("No locations specified when starting "+entity());
//        if (locations.size() != 1 || Iterables.getOnlyElement(locations)==null)
//            throw new IllegalArgumentException("Ambiguous locations detected when starting "+entity()+": "+locations);
//        return Iterables.getOnlyElement(locations);
//    }
//
//    /** runs the tasks needed to start, wrapped by setting {@link Attributes#SERVICE_STATE_EXPECTED} appropriately */
//    public void start(Collection<? extends Location> locations) {
//
//        //This method was copied from CloudFoundryEntityImpl.doStart()
//        ServiceStateLogic.setExpectedState(entity(), Lifecycle.STARTING);
//        try {
//            entity().getDriver().start();
//            ServiceStateLogic.setExpectedState(entity(), Lifecycle.RUNNING);
//        } catch (Throwable t) {
//            ServiceStateLogic.setExpectedState(entity(), Lifecycle.ON_FIRE);
//            log.error("Error error starting entity {}", entity());
//            throw Exceptions.propagate(t);
//        }
//    }
//
//    protected void startInLocations(Collection<? extends Location> locations) {
//        startInLocation(getLocation(locations));
//    }
//
//    /** Dispatches to the appropriate method(s) to start in the given location. */
//    protected void startInLocation(final Location location) {
//        Supplier<MachineLocation> locationS = null;
//        if (location instanceof MachineProvisioningLocation) {
//            Task<MachineLocation> machineTask = provisionAsync((MachineProvisioningLocation<?>)location);
//            locationS = Tasks.supplier(machineTask);
//        } else if (location instanceof MachineLocation) {
//            locationS = Suppliers.ofInstance((MachineLocation) location);
//        }
//        Preconditions.checkState(locationS != null, "Unsupported location " + location + ", when starting " + entity());
//
//        final Supplier<MachineLocation> locationSF = locationS;
//        preStartAtMachineAsync(locationSF);
//        DynamicTasks.queue("start (processes)", new Runnable() { public void run() {
//            startProcessesAtMachine(locationSF);
//        }});
//        postStartAtMachineAsync();
//        return;
//    }
//
//    /**
//     * Returns a queued {@link Task} which provisions a machine in the given location
//     * and returns that machine. The task can be used as a supplier to subsequent methods.
//     */
//    protected Task<MachineLocation> provisionAsync(final MachineProvisioningLocation<?> location) {
//        return DynamicTasks.queue(Tasks.<MachineLocation>builder().name("provisioning ("+location.getDisplayName()+")").body(
//                new Callable<MachineLocation>() {
//                    public MachineLocation call() throws Exception {
//                        // Blocks if a latch was configured.
//                        entity().getConfig(BrooklynConfigKeys.PROVISION_LATCH);
//                        final Map<String,Object> flags = obtainFlagsForLocation(location);
//                        if (!(location instanceof LocalhostMachineProvisioningLocation))
//                            log.info("Starting {}, obtaining a new location instance in {} with ports {}", new Object[] {entity(), location, flags.get("inboundPorts")});
//                        entity().setAttribute(SoftwareProcess.PROVISIONING_LOCATION, location);
//                        MachineLocation machine;
//                        try {
//                            machine = Tasks.withBlockingDetails("Provisioning machine in "+location, new Callable<MachineLocation>() {
//                                public MachineLocation call() throws NoMachinesAvailableException {
//                                    return location.obtain(flags);
//                                }});
//                            if (machine == null) throw new NoMachinesAvailableException("Failed to obtain machine in "+location.toString());
//                        } catch (Exception e) {
//                            throw Exceptions.propagate(e);
//                        }
//
//                        if (log.isDebugEnabled())
//                            log.debug("While starting {}, obtained new location instance {}", entity(),
//                                    (machine instanceof SshMachineLocation ?
//                                            machine+", details "+((SshMachineLocation)machine).getUser()+":"+Sanitizer.sanitize(((SshMachineLocation)machine).config().getLocalBag())
//                                            : machine));
//                        return machine;
//                    }
//                }).build());
//    }
//
//    /** Wraps a call to {@link #preStartCustom(MachineLocation)}, after setting the hostname and address. */
//    protected void preStartAtMachineAsync(final Supplier<MachineLocation> machineS) {
//        DynamicTasks.queue("pre-start", new Runnable() { public void run() {
//            MachineLocation machine = machineS.get();
//            log.info("Starting {} on machine {}", entity(), machine);
//            Collection<Location> oldLocs = entity().getLocations();
//            if (!oldLocs.isEmpty()) {
//                List<MachineLocation> oldSshLocs = ImmutableList.copyOf(Iterables.filter(oldLocs, MachineLocation.class));
//                if (!oldSshLocs.isEmpty()) {
//                    // check if existing locations are compatible
//                    log.debug("Entity "+entity()+" had machine locations "+oldSshLocs+" when starting at "+machine+"; checking if they are compatible");
//                    for (MachineLocation oldLoc: oldSshLocs) {
//                        // machines are deemed compatible if hostname and address are the same, or they are localhost
//                        // this allows a machine create by jclouds to then be defined with an ip-based spec
//                        if (!"localhost".equals(machine.getConfig(AbstractLocation.ORIGINAL_SPEC))) {
//                            checkLocationParametersCompatible(machine, oldLoc, "hostname",
//                                    oldLoc.getAddress().getHostName(), machine.getAddress().getHostName());
//                            checkLocationParametersCompatible(machine, oldLoc, "address",
//                                    oldLoc.getAddress().getHostAddress(), machine.getAddress().getHostAddress());
//                        }
//                    }
//                    log.debug("Entity "+entity()+" old machine locations "+oldSshLocs+" were compatible, removing them to start at "+machine);
//                    entity().removeLocations(oldSshLocs);
//                }
//            }
//            entity().addLocations(ImmutableList.of((Location)machine));
//
//            // elsewhere we rely on (public) hostname being set _after_ subnet_hostname
//            // (to prevent the tiny possibility of races resulting in hostname being returned
//            // simply because subnet is still being looked up)
//            Maybe<String> lh = Machines.getSubnetHostname(machine);
//            Maybe<String> la = Machines.getSubnetIp(machine);
//            if (lh.isPresent()) entity().setAttribute(Attributes.SUBNET_HOSTNAME, lh.get());
//            if (la.isPresent()) entity().setAttribute(Attributes.SUBNET_ADDRESS, la.get());
//            entity().setAttribute(Attributes.HOSTNAME, machine.getAddress().getHostName());
//            entity().setAttribute(Attributes.ADDRESS, machine.getAddress().getHostAddress());
//            if (machine instanceof SshMachineLocation) {
//                @SuppressWarnings("resource")
//                SshMachineLocation sshMachine = (SshMachineLocation) machine;
//                UserAndHostAndPort sshAddress = UserAndHostAndPort.fromParts(sshMachine.getUser(), sshMachine.getAddress().getHostName(), sshMachine.getPort());
//                entity().setAttribute(Attributes.SSH_ADDRESS, sshAddress);
//            }
//
//            resolveOnBoxDir(entity(), machine);
//            preStartCustom(machine);
//        }});
//    }
//
//    /**
//     * Resolves the on-box dir.
//     * <p>
//     * Initialize and pre-create the right onbox working dir, if an ssh machine location.
//     * Logs a warning if not.
//     */
//    @SuppressWarnings("deprecation")
//    public static String resolveOnBoxDir(EntityInternal entity, MachineLocation machine) {
//        String base = entity.getConfig(BrooklynConfigKeys.ONBOX_BASE_DIR);
//        if (base==null) base = machine.getConfig(BrooklynConfigKeys.ONBOX_BASE_DIR);
//        if (base!=null && Boolean.TRUE.equals(entity.getConfig(ON_BOX_BASE_DIR_RESOLVED))) return base;
//        if (base==null) base = entity.getManagementContext().getConfig().getConfig(BrooklynConfigKeys.ONBOX_BASE_DIR);
//        if (base==null) base = entity.getConfig(BrooklynConfigKeys.BROOKLYN_DATA_DIR);
//        if (base==null) base = machine.getConfig(BrooklynConfigKeys.BROOKLYN_DATA_DIR);
//        if (base==null) base = entity.getManagementContext().getConfig().getConfig(BrooklynConfigKeys.BROOKLYN_DATA_DIR);
//        if (base==null) base = "~/brooklyn-managed-processes";
//        if (base.equals("~")) base=".";
//        if (base.startsWith("~/")) base = "."+base.substring(1);
//
//        String resolvedBase = null;
//        if (entity.getConfig(BrooklynConfigKeys.SKIP_ON_BOX_BASE_DIR_RESOLUTION) || machine.getConfig(BrooklynConfigKeys.SKIP_ON_BOX_BASE_DIR_RESOLUTION)) {
//            if (log.isDebugEnabled()) log.debug("Skipping on-box base dir resolution for "+entity+" at "+machine);
//            if (!Os.isAbsolutish(base)) base = "~/"+base;
//            resolvedBase = Os.tidyPath(base);
//        } else if (machine instanceof SshMachineLocation) {
//            SshMachineLocation ms = (SshMachineLocation)machine;
//            ProcessTaskWrapper<Integer> baseTask = SshEffectorTasks.ssh(
//                    BashCommands.alternatives("mkdir -p \"${BASE_DIR}\"",
//                            BashCommands.chain(
//                                    BashCommands.sudo("mkdir -p \"${BASE_DIR}\""),
//                                    BashCommands.sudo("chown " + ms.getUser() + " \"${BASE_DIR}\""))),
//                    "cd ~",
//                    "cd ${BASE_DIR}",
//                    "echo BASE_DIR_RESULT':'`pwd`:BASE_DIR_RESULT")
//                    .environmentVariable("BASE_DIR", base)
//                    .requiringExitCodeZero()
//                    .summary("initializing on-box base dir "+base).newTask();
//            DynamicTasks.queueIfPossible(baseTask).orSubmitAsync(entity);
//            resolvedBase = Strings.getFragmentBetween(baseTask.block().getStdout(), "BASE_DIR_RESULT:", ":BASE_DIR_RESULT");
//        }
//        if (resolvedBase==null) {
//            if (!Os.isAbsolutish(base)) base = "~/"+base;
//            resolvedBase = Os.tidyPath(base);
//            log.warn("Could not resolve on-box directory for "+entity+" at "+machine+"; using "+resolvedBase+", though this may not be accurate at the target (and may fail shortly)");
//        }
//        entity.setConfig(BrooklynConfigKeys.ONBOX_BASE_DIR, resolvedBase);
//        entity.setConfig(ON_BOX_BASE_DIR_RESOLVED, true);
//        return resolvedBase;
//    }
//
//    protected void checkLocationParametersCompatible(MachineLocation oldLoc, MachineLocation newLoc, String paramSummary,
//                                                     Object oldParam, Object newParam) {
//        if (oldParam==null || newParam==null || !oldParam.equals(newParam))
//            throw new IllegalStateException("Cannot start "+entity()+" in "+newLoc+" as it has already been started with incompatible location "+oldLoc+" " +
//                    "("+paramSummary+" not compatible: "+oldParam+" / "+newParam+"); "+newLoc+" may require manual removal.");
//    }
//
//    /**
//     * Default pre-start hooks.
//     * <p>
//     * Can be extended by subclasses if needed.
//     */
//    protected void preStartCustom(MachineLocation machine) {
//        ConfigToAttributes.apply(entity());
//
//        // Opportunity to block startup until other dependent components are available
//        Object val = entity().getConfig(SoftwareProcess.START_LATCH);
//        if (val != null) log.debug("{} finished waiting for start-latch; continuing...", entity(), val);
//    }
//
//    protected Map<String, Object> obtainFlagsForLocation(final MachineProvisioningLocation<?> location) {
//        if (entity() instanceof ProvidesProvisioningFlags) {
//            return ((ProvidesProvisioningFlags)entity()).obtainFlagsForLocation(location).getAllConfig();
//        }
//        return MutableMap.<String, Object>of();
//    }
//
//    protected abstract String startProcessesAtMachine(final Supplier<MachineLocation> machineS);
//
//    protected void postStartAtMachineAsync() {
//        DynamicTasks.queue("post-start", new Runnable() { public void run() {
//            postStartCustom();
//        }});
//    }
//
//    /**
//     * Default post-start hooks.
//     * <p>
//     * Can be extended by subclasses, and typically will wait for confirmation of start.
//     * The service not set to running until after this. Also invoked following a restart.
//     */
//    protected void postStartCustom() {
//        // nothing by default
//    }
//
//    /** @deprecated since 0.7.0 use {@link #restart(ConfigBag)} */
//    @Deprecated
//    public void restart() {
//        restart(ConfigBag.EMPTY);
//    }
//
//    /**
//     * whether when 'auto' mode is specified, the machine should be stopped when the restart effector is called
//     * <p>
//     * with {@link MachineLifecycleEffectorTasks}, a machine will always get created on restart if there wasn't one already
//     * (unlike certain subclasses which might attempt a shortcut process-level restart)
//     * so there is no reason for default behaviour of restart to throw away a provisioned machine,
//     * hence default impl returns <code>false</code>.
//     * <p>
//     * if it is possible to tell that a machine is unhealthy, or if {@link #restart(ConfigBag)} is overridden,
//     * then it might be appropriate to return <code>true</code> here.
//     */
//    protected boolean getDefaultRestartStopsMachine() {
//        return false;
//    }
//
//    /**
//     * Default restart implementation for an entity.
//     * <p>
//     * Stops processes if possible, then starts the entity again.
//     */
//    public void restart(ConfigBag parameters) {
//        ServiceStateLogic.setExpectedState(entity(), Lifecycle.STOPPING);
//
//        SoftwareProcess.RestartSoftwareParameters.RestartMachineMode isRestartMachine = parameters.get(SoftwareProcess.RestartSoftwareParameters.RESTART_MACHINE_TYPED);
//        if (isRestartMachine==null)
//            isRestartMachine= SoftwareProcess.RestartSoftwareParameters.RestartMachineMode.AUTO;
//        if (isRestartMachine== SoftwareProcess.RestartSoftwareParameters.RestartMachineMode.AUTO)
//            isRestartMachine = getDefaultRestartStopsMachine() ? SoftwareProcess.RestartSoftwareParameters.RestartMachineMode.TRUE : SoftwareProcess.RestartSoftwareParameters.RestartMachineMode.FALSE;
//
//        DynamicTasks.queue("pre-restart", new Runnable() { public void run() {
//            //Calling preStopCustom without a corresponding postStopCustom invocation
//            //doesn't look right so use a separate callback pair; Also depending on the arguments
//            //stop() could be called which will call the {pre,post}StopCustom on its own.
//            preRestartCustom();
//        }});
//
//        if (isRestartMachine== SoftwareProcess.RestartSoftwareParameters.RestartMachineMode.FALSE) {
//            DynamicTasks.queue("stopping (process)", new Callable<String>() { public String call() {
//                DynamicTasks.markInessential();
//                stopProcessesAtMachine();
//                DynamicTasks.waitForLast();
//                return "Stop of process completed with no errors.";
//            }});
//        } else {
//            DynamicTasks.queue("stopping (machine)", new Callable<String>() { public String call() {
//                DynamicTasks.markInessential();
//                stop(ConfigBag.newInstance().configure(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE, SoftwareProcess.StopSoftwareParameters.StopMode.IF_NOT_STOPPED));
//                DynamicTasks.waitForLast();
//                return "Stop of machine completed with no errors.";
//            }});
//        }
//
//        DynamicTasks.queue("starting", new Runnable() { public void run() {
//            // startInLocations will look up the location, and provision a machine if necessary
//            // (if it remembered the provisioning location)
//            ServiceStateLogic.setExpectedState(entity(), Lifecycle.STARTING);
//            startInLocations(null);
//        }});
//
//        restartChildren(parameters);
//
//        DynamicTasks.queue("post-restart", new Runnable() { public void run() {
//            postRestartCustom();
//        }});
//
//        DynamicTasks.waitForLast();
//        ServiceStateLogic.setExpectedState(entity(), Lifecycle.RUNNING);
//    }
//
//    protected void restartChildren(ConfigBag parameters) {
//        // TODO should we consult ChildStartableMode?
//
//        Boolean isRestartChildren = parameters.get(SoftwareProcess.RestartSoftwareParameters.RESTART_CHILDREN);
//        if (isRestartChildren==null || !isRestartChildren) {
//            return;
//        }
//
//        if (isRestartChildren) {
//            DynamicTasks.queue(StartableMethods.restartingChildren(entity(), parameters));
//            return;
//        }
//
//        throw new IllegalArgumentException("Invalid value '"+isRestartChildren+"' for "+ SoftwareProcess.RestartSoftwareParameters.RESTART_CHILDREN.getName());
//    }
//
//    /** @deprecated since 0.7.0 use {@link #stop(ConfigBag)} */
//    @Deprecated
//    public void stop() {
//        stop(ConfigBag.EMPTY);
//    }
//
//    /**
//     * Default stop implementation for an entity.
//     * <p>
//     * Aborts if already stopped, otherwise sets state {@link Lifecycle#STOPPING} then
//     * invokes {@link #preStopCustom()}, {@link #stopProcessesAtMachine()}, then finally
//     * {@link #stopAnyProvisionedMachines()} and sets state {@link Lifecycle#STOPPED}.
//     * If no errors were encountered call {@link #postStopCustom()} at the end.
//     */
//    public void stop(ConfigBag parameters) {
//        preStopConfirmCustom();
//
//        log.info("Stopping {} in {}", entity(), entity().getLocations());
//
//        SoftwareProcess.StopSoftwareParameters.StopMode stopMachineMode = getStopMachineMode(parameters);
//        SoftwareProcess.StopSoftwareParameters.StopMode stopProcessMode = parameters.get(SoftwareProcess.StopSoftwareParameters.STOP_PROCESS_MODE);
//
//        DynamicTasks.queue("pre-stop", new Callable<String>() { public String call() {
//            if (entity().getAttribute(SoftwareProcess.SERVICE_STATE_ACTUAL)==Lifecycle.STOPPED) {
//                log.debug("Skipping stop of entity "+entity()+" when already stopped");
//                return "Already stopped";
//            }
//            ServiceStateLogic.setExpectedState(entity(), Lifecycle.STOPPING);
//            entity().setAttribute(SoftwareProcess.SERVICE_UP, false);
//            preStopCustom();
//            return null;
//        }});
//
//        Maybe<MachineLocation> machine = Machines.findUniqueMachineLocation(entity().getLocations());
//        Task<String> stoppingProcess = null;
//        if (canStop(stopProcessMode, entity())) {
//            stoppingProcess = DynamicTasks.queue("stopping (process)", new Callable<String>() { public String call() {
//                DynamicTasks.markInessential();
//                stopProcessesAtMachine();
//                DynamicTasks.waitForLast();
//                return "Stop at machine completed with no errors.";
//            }});
//        }
//
//        Task<StopMachineDetails<Integer>> stoppingMachine = null;
//        if (canStop(stopMachineMode, machine.isAbsent())) {
//            // Release this machine (even if error trying to stop process - we rethrow that after)
//            stoppingMachine = DynamicTasks.queue("stopping (machine)", new Callable<StopMachineDetails<Integer>>() {
//                public StopMachineDetails<Integer> call() {
//                    return stopAnyProvisionedMachines();
//                }
//            });
//
//            DynamicTasks.drain(entity().getConfig(STOP_PROCESS_TIMEOUT), false);
//
//            // shutdown the machine if stopping process fails or takes too long
//            synchronized (stoppingMachine) {
//                // task also used as mutex by DST when it submits it; ensure it only submits once!
//                if (!stoppingMachine.isSubmitted()) {
//                    // force the stoppingMachine task to run by submitting it here
//                    StringBuilder msg = new StringBuilder("Submitting machine stop early in background for ").append(entity());
//                    if (stoppingProcess == null) {
//                        msg.append(". Process stop skipped, pre-stop not finished?");
//                    } else {
//                        msg.append(" because process stop has ").append(
//                                (stoppingProcess.isDone() ? "finished abnormally" : "not finished"));
//                    }
//                    log.warn(msg.toString());
//                    Entities.submit(entity(), stoppingMachine);
//                }
//            }
//        }
//
//        try {
//            // This maintains previous behaviour of silently squashing any errors on the stoppingProcess task if the
//            // stoppingMachine exits with a nonzero value
//            boolean checkStopProcesses = (stoppingProcess != null && (stoppingMachine == null || stoppingMachine.get().value == 0));
//
//            if (checkStopProcesses) {
//                // TODO we should test for destruction above, not merely successful "stop", as things like localhost and ssh won't be destroyed
//                DynamicTasks.waitForLast();
//                if (machine.isPresent()) {
//                    // throw early errors *only if* there is a machine and we have not destroyed it
//                    stoppingProcess.get();
//                }
//            }
//        } catch (Throwable e) {
//            ServiceStateLogic.setExpectedState(entity(), Lifecycle.ON_FIRE);
//            Exceptions.propagate(e);
//        }
//        entity().setAttribute(SoftwareProcess.SERVICE_UP, false);
//        ServiceStateLogic.setExpectedState(entity(), Lifecycle.STOPPED);
//
//        DynamicTasks.queue("post-stop", new Callable<Void>() { public Void call() {
//            postStopCustom();
//            return null;
//        }});
//
//        if (log.isDebugEnabled()) log.debug("Stopped software process entity "+entity());
//    }
//
//    public static SoftwareProcess.StopSoftwareParameters.StopMode getStopMachineMode(ConfigBag parameters) {
//        @SuppressWarnings("deprecation")
//        final boolean hasStopMachine = parameters.containsKey(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE);
//        @SuppressWarnings("deprecation")
//        final Boolean isStopMachine = parameters.get(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE);
//
//        final boolean hasStopMachineMode = parameters.containsKey(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE);
//        final SoftwareProcess.StopSoftwareParameters.StopMode stopMachineMode = parameters.get(SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE);
//
//        if (hasStopMachine && isStopMachine != null) {
//            checkCompatibleMachineModes(isStopMachine, hasStopMachineMode, stopMachineMode);
//            if (isStopMachine) {
//                return SoftwareProcess.StopSoftwareParameters.StopMode.IF_NOT_STOPPED;
//            } else {
//                return SoftwareProcess.StopSoftwareParameters.StopMode.NEVER;
//            }
//        }
//        return stopMachineMode;
//    }
//
//    public static boolean canStop(SoftwareProcess.StopSoftwareParameters.StopMode stopMode, Entity entity) {
//        boolean isEntityStopped = entity.getAttribute(SoftwareProcess.SERVICE_STATE_ACTUAL)==Lifecycle.STOPPED;
//        return canStop(stopMode, isEntityStopped);
//    }
//
//    protected static boolean canStop(SoftwareProcess.StopSoftwareParameters.StopMode stopMode, boolean isStopped) {
//        return stopMode == SoftwareProcess.StopSoftwareParameters.StopMode.ALWAYS ||
//                stopMode == SoftwareProcess.StopSoftwareParameters.StopMode.IF_NOT_STOPPED && !isStopped;
//    }
//
//    @SuppressWarnings("deprecation")
//    private static void checkCompatibleMachineModes(Boolean isStopMachine, boolean hasStopMachineMode, SoftwareProcess.StopSoftwareParameters.StopMode stopMachineMode) {
//        if (hasStopMachineMode &&
//                (isStopMachine && stopMachineMode != SoftwareProcess.StopSoftwareParameters.StopMode.IF_NOT_STOPPED ||
//                        !isStopMachine && stopMachineMode != SoftwareProcess.StopSoftwareParameters.StopMode.NEVER)) {
//            throw new IllegalStateException("Incompatible values for " +
//                    SoftwareProcess.StopSoftwareParameters.STOP_MACHINE.getName() + " (" + isStopMachine + ") and " +
//                    SoftwareProcess.StopSoftwareParameters.STOP_MACHINE_MODE.getName() + " (" + stopMachineMode + "). " +
//                    "Use only one of the parameters.");
//        }
//    }
//
//    /**
//     * Override to check whether stop can be executed.
//     * Throw if stop should be aborted.
//     */
//    protected void preStopConfirmCustom() {
//        // nothing needed here
//    }
//
//    protected void preStopCustom() {
//        // nothing needed here
//    }
//
//    protected void postStopCustom() {
//        // nothing needed here
//    }
//
//    protected void preRestartCustom() {
//        // nothing needed here
//    }
//
//    protected void postRestartCustom() {
//        // nothing needed here
//    }
//
//    public static class StopMachineDetails<T> implements Serializable {
//        private static final long serialVersionUID = 3256747214315895431L;
//        final String message;
//        final T value;
//        protected StopMachineDetails(String message, T value) {
//            this.message = message;
//            this.value = value;
//        }
//        @Override
//        public String toString() {
//            return message;
//        }
//    }
//
//    /**
//     * Return string message of result.
//     * <p>
//     * Can run synchronously or not, caller will submit/queue as needed, and will block on any submitted tasks.
//     */
//    protected abstract String stopProcessesAtMachine();
//
//    /**
//     * Stop the {@link MachineLocation} the entity is provisioned at.
//     * <p>
//     * Can run synchronously or not, caller will submit/queue as needed, and will block on any submitted tasks.
//     */
//    protected StopMachineDetails<Integer> stopAnyProvisionedMachines() {
//        @SuppressWarnings("unchecked")
//        MachineProvisioningLocation<MachineLocation> provisioner = entity().getAttribute(SoftwareProcess.PROVISIONING_LOCATION);
//
//        if (Iterables.isEmpty(entity().getLocations())) {
//            log.debug("No machine decommissioning necessary for "+entity()+" - no locations");
//            return new StopMachineDetails<Integer>("No machine decommissioning necessary - no locations", 0);
//        }
//
//        // Only release this machine if we ourselves provisioned it (e.g. it might be running other services)
//        if (provisioner==null) {
//            log.debug("No machine decommissioning necessary for "+entity()+" - did not provision");
//            return new StopMachineDetails<Integer>("No machine decommissioning necessary - did not provision", 0);
//        }
//
//        Location machine = getLocation(null);
//        if (!(machine instanceof MachineLocation)) {
//            log.debug("No decommissioning necessary for "+entity()+" - not a machine location ("+machine+")");
//            return new StopMachineDetails<Integer>("No machine decommissioning necessary - not a machine ("+machine+")", 0);
//        }
//
//        try {
//            entity().removeLocations(ImmutableList.of(machine));
//            entity().setAttribute(Attributes.HOSTNAME, null);
//            entity().setAttribute(Attributes.ADDRESS, null);
//            entity().setAttribute(Attributes.SUBNET_HOSTNAME, null);
//            entity().setAttribute(Attributes.SUBNET_ADDRESS, null);
//            if (provisioner != null) provisioner.release((MachineLocation)machine);
//        } catch (Throwable t) {
//            throw Exceptions.propagate(t);
//        }
//        return new StopMachineDetails<Integer>("Decommissioned "+machine, 1);
//    }

}