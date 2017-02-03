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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesCompositeTask;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

/**
 * @author emmartins
 */
public class UpdateSubsystemConfigurationTaskBuilder<S> extends ResourcesCompositeTask.Builder<S, ManageableResource> {

    public UpdateSubsystemConfigurationTaskBuilder(String subsystemName, UpdateSubsystemConfigurationSubtaskBuilder<S>... subtasks) {
        name(new ServerMigrationTaskName.Builder("update-subsystem").addAttribute("name", subsystemName).build());
        skipPolicy(TaskSkipPolicy.skipByTaskEnvironment(EnvironmentProperties.getSubsystemTaskPropertiesPrefix(subsystemName)));
        beforeRun(context -> context.getLogger().infof("Updating subsystem %s configuration(s)...", subsystemName));
        final ResourceCompositeSubtasks.Builder<S, SubsystemConfiguration> subtasksBuilder = new ResourceCompositeSubtasks.Builder<>();
        for (UpdateSubsystemConfigurationSubtaskBuilder<S> subtask : subtasks) {
            subtasksBuilder.subtask(subtask);
        }
        subtasks(SubsystemConfiguration.class, subsystemName, subtasksBuilder);
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Subsystem %s configuration(s) updated.", subsystemName);
            } else {
                context.getLogger().infof("No subsystem %s configuration(s) updated.", subsystemName);
            }
        });
    }
}
