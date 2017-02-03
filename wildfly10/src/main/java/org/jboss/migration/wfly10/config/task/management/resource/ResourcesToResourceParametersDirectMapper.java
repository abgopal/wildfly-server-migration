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

package org.jboss.migration.wfly10.config.task.management.resource;

import org.jboss.migration.core.task.component.BuildParameters;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesBuildParameters;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ResourcesToResourceParametersDirectMapper<S, R extends ManageableResource> implements BuildParameters.Mapper<ResourcesBuildParameters<S, R>, ResourceBuildParameters<S, R>> {
    @Override
    public Collection<ResourceBuildParameters<S, R>> apply(ResourcesBuildParameters<S, R> tParams) {
        return tParams.getResources().stream().map(resource -> new ResourceBuildParametersImpl<S, R>(tParams.getSource(), tParams.getServerConfiguration(), resource)).collect(toSet());
    }
}
