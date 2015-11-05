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
package org.wildfly.migration.cli.logger;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author emmartins
 */
@MessageLogger(projectCode = "WFMIGRCLI") //todo: proper project code?
public interface CommandLineMigrationLogger extends BasicLogger {

    CommandLineMigrationLogger ROOT_LOGGER = Logger.getMessageLogger(CommandLineMigrationLogger.class, CommandLineMigrationLogger.class.getPackage().getName());

    /**
     * Instructions for the {@link org.wildfly.migration.cli.CommandLineConstants#SOURCE} command line arguments.
     *
     * @return the message.
     */
    @Message(id = Message.NONE, value = "Path to the source server configuration file to migrate from.")
    String argSource();

    /**
     * Instructions for the {@link org.wildfly.migration.cli.CommandLineConstants#TARGET} command line arguments.
     *
     * @return the message.
     */
    @Message(id = Message.NONE, value = "Path to the base dir of the server to migrate to.")
    String argTarget();

    @Message(id = Message.NONE, value = "Usage: %s [args...]%nwhere args include:")
    String argUsage(String executableName);
}
