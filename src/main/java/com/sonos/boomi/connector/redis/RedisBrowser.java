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

package com.sonos.boomi.connector.redis;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseBrowser;
import com.boomi.connector.util.BaseConnection;
import com.sonos.boomi.connector.redis.object.RedisObjectTypes;

import java.util.Collection;

/**
 * Implementation of BaseBrowser that provides Browse and Test Connection support to Boomi
 */
public class RedisBrowser extends BaseBrowser implements ConnectionTester {

    /**
     * @param context BrowseContext to provide to {@link com.boomi.connector.util.BaseBrowser}
     */
    protected RedisBrowser(BrowseContext context) {
        super(context);
    }

    /**
     * @param connection Connection to provide to {@link com.boomi.connector.util.BaseBrowser}
     */
    protected RedisBrowser(BaseConnection<BrowseContext> connection) {
        super(connection);
    }

    /**
     * @return Returns all object types loaded from object type metadata resource file
     */
    @Override
    public ObjectTypes getObjectTypes() {
        return RedisObjectTypes.getInstance().getBoomiTypes();
    }

    /**
     * @param s Object type used when searching for object definitions to return
     * @param collection Roles to use when searching for object definitions to return
     * @return Returns a new instance of {@link com.boomi.connector.api.ObjectDefinitions} containing all definitions
     * for the object type and roles
     */
    @Override
    public ObjectDefinitions getObjectDefinitions(String s, Collection<ObjectDefinitionRole> collection) {
        OperationType operationType = getContext().getOperationType();
        String customType = getContext().getCustomOperationType();
        return RedisObjectTypes
                .getInstance()
                .getObjectType(s)
                .toObjectDefinitions(operationType.name(), customType, collection);
    }

    /**
     * Tests the Redis connection by executing a PING command
     */
    @Override
    public void testConnection() {
        RedisConnection connection = (RedisConnection) getConnection();
        try {
            String pong = connection.getConnection().sync().ping();
            if (!"PONG".equals(pong)) {
                throw new ConnectorException("Connection did not respond to PING with 'PONG'");
            }
        } finally {
            connection.closeConnection();
        }
    }
}
