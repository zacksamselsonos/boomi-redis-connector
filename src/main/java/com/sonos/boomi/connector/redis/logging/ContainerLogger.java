/*
 * Copyright 2020 Sonos, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sonos.boomi.connector.redis.logging;

import java.util.logging.Logger;

/**
 * Provides a singleton Logger object for Atom-container logging
 */
public class ContainerLogger {

    private final static String LOGGER_SUBSYSTEM_NAME = "com.sonos.boomi.connector.redis.RedisConnector";
    private final static String RUNTIME_EX = "This class is a wrapper class and should not be instantiated.";

    private static volatile Logger singleton;

    /**
     * Throws a {@link java.lang.RuntimeException}
     */
    private ContainerLogger(){
        throw new RuntimeException(RUNTIME_EX);
    }

    /**
     * @return Returns the singleton instance of Logger used for container-level logging
     */
    public static Logger getInstance() {
        if (singleton == null) {
            synchronized (ContainerLogger.class) {
                if (singleton == null)
                    singleton = Logger.getLogger(LOGGER_SUBSYSTEM_NAME);
            }
        }

        return singleton;
    }

}
