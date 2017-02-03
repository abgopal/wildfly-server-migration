/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesCompositeTask;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Adds private interface to config, and updates jgroup socket bindings to use it.
 * @author emmartins
 */
public class AddPrivateInterfaceTaskBuilder<S> extends ServerConfigurationCompositeTask.Builder<S> {

    private static final String INTERFACE_NAME = "private";
    private static final String[] SOCKET_BINDING_NAMES = {"jgroups-mping", "jgroups-tcp", "jgroups-tcp-fd", "jgroups-udp", "jgroups-udp-fd"};

    public AddPrivateInterfaceTaskBuilder() {
        name("setup-private-interface");
        skipPolicy((params, name) -> context -> {
            if (TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(name).isSkipped(context)) {
                return true;
            }
            for (String socketBindingName : SOCKET_BINDING_NAMES) {
                if (!params.getServerConfiguration().findResources(SocketBindingResource.class, socketBindingName).isEmpty()) {
                    return false;
                }
            }
            return true;
        });
        beforeRun(context -> context.getLogger().infof("Private interface setup starting..."));
        subtasks(new ServerConfigurationCompositeSubtasks.Builder<S>()
                .subtask(new AddInterface<>())
                .subtask(SocketBindingGroupResource.class, new UpdateSocketBindingGroups<>()));
        afterRun(context -> context.getLogger().infof("Private interface setup done."));
    }

    protected static class AddInterface<S> extends ServerConfigurationLeafTask.Builder<S> {
        protected AddInterface() {
            name("add-interface");
            run((params, taskName) -> context -> {
                final ManageableServerConfiguration serverConfiguration = params.getServerConfiguration();
                if (serverConfiguration.getInterfaceResourceNames().contains(INTERFACE_NAME)) {
                    context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final ModelNode addInterfaceOp = Util.createAddOperation(serverConfiguration.getInterfaceResourcePathAddress(INTERFACE_NAME));
                addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                serverConfiguration.executeManagementOperation(addInterfaceOp);
                context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            });
        }
    }

    protected static class UpdateSocketBindingGroups<S> extends ResourcesCompositeTask.Builder<S, SocketBindingGroupResource> {
        protected UpdateSocketBindingGroups() {
            name("update-socket-binding-groups");
            subtasks(new ResourceCompositeSubtasks.Builder<S, SocketBindingGroupResource>().subtask(new UpdateSocketBindingGroup<>()));
        }
    }

    protected static class UpdateSocketBindingGroup<S> extends ResourceLeafTask.Builder<S, SocketBindingGroupResource> {
        protected UpdateSocketBindingGroup() {
            name(params -> new ServerMigrationTaskName.Builder("update-socket-binding-group").addAttribute("name", params.getResource().getResourceName()).build());
            run((params, taskName) -> context -> {
                final List<String> updated = new ArrayList<>();
                for (String socketBinding : SOCKET_BINDING_NAMES) {
                    SocketBindingResource socketBindingResource = params.getResource().getSocketBindingResource(socketBinding);
                    ModelNode config = socketBindingResource.getResourceConfiguration();
                    if (config != null) {
                        if (!config.hasDefined(INTERFACE) || !config.get(INTERFACE).asString().equals(INTERFACE_NAME)) {
                            final PathAddress pathAddress = socketBindingResource.getResourcePathAddress();
                            final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                            writeAttrOp.get(NAME).set(INTERFACE);
                            writeAttrOp.get(VALUE).set(INTERFACE_NAME);
                            socketBindingResource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                            context.getLogger().infof("Socket binding %s interface set to %s", pathAddress.toCLIStyleString(), INTERFACE_NAME);
                            updated.add(socketBinding);
                        }
                    }
                }
                if (updated.isEmpty()) {
                    return ServerMigrationTaskResult.SKIPPED;
                } else {
                    return new ServerMigrationTaskResult.Builder().success().addAttribute("updated", updated.toString()).build();
                }
            });
        }
    }
}