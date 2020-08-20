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

package com.sonos.boomi.connector.redis.operation;

import com.boomi.connector.api.ObjectData;
import com.boomi.connector.util.BaseUpdateOperation;
import com.sonos.boomi.connector.redis.RedisConnection;
import com.sonos.boomi.connector.redis.util.IntegerUtil;
import com.sonos.boomi.connector.redis.util.OperationUtil;

import java.util.Map;

/**
 * Responsible for all common logic used during UPSERT operations
 */
public abstract class BaseRedisUpsertOperation extends BaseUpdateOperation {

    protected final RedisConnection _connection;

    /**
     * @param connection Connection provided by the Connector
     */
    protected BaseRedisUpsertOperation(RedisConnection connection) {
        super(connection);

        _connection = connection;
    }

    /**
     * Closes the Redis connection
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        super.finalize();

        if (_connection != null) {
            _connection.closeConnection();
        }
    }

    /**
     * @param objectData ObjectData containing the operation property named 'key'
     * @return Returns the key to use for the provided object data. Returns null if no
     * operation property named 'key' is found.
     */
    protected String getKey(ObjectData objectData) {
        Map<String, String> properties = objectData.getDynamicProperties();
        String key = properties.get("key");
        if (key == null || key.length() == 0) {
            return null;
        }
        return formatKey(key);
    }

    /**
     * @param objectData ObjectData containing the operation property named 'ttl'
     * @return Returns the time-to-live (ttl) to use for the provided object data.
     * Returns null if no operation property named 'ttl' is found.
     */
    protected Integer getTtl(ObjectData objectData) {
        String ttlVal = objectData.getDynamicProperties().get("ttl");
        return IntegerUtil.tryParseInt(ttlVal, -1);
    }

    /**
     * @param key Key to format
     * @return Returns a formatted key using any key prefix provided by the operation
     */
    protected String formatKey(String key) {
        return OperationUtil.getPrefixedKey(key, getContext());
    }

    /**
     * @return Returns the Redis connection object on this instance
     */
    protected RedisConnection getRedisConnection() {
        return _connection;
    }
}
