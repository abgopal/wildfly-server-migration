/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.cli;

import org.jboss.migration.cli.logger.CommandLineMigrationLogger;

import java.io.PrintStream;

/**
 * @author emmartins
 */
public class CommandLineArgumentUsageImpl extends CommandLineArgumentUsage {

    public static void init(){

        addArguments(CommandLineConstants.ENVIRONMENT + " <value>");
        instructions.add(CommandLineMigrationLogger.ROOT_LOGGER.argEnvironment());

        addArguments(CommandLineConstants.INTERACTIVE + " <value>");
        instructions.add(CommandLineMigrationLogger.ROOT_LOGGER.argInteractive());

        addArguments(CommandLineConstants.SOURCE + " <value>");
        instructions.add(CommandLineMigrationLogger.ROOT_LOGGER.argSource());

        addArguments(CommandLineConstants.TARGET + " <value>");
        instructions.add(CommandLineMigrationLogger.ROOT_LOGGER.argTarget());
    }

    public static void printUsage(final PrintStream out) {
        init();
        out.print(usage("server-migration"));
    }
}