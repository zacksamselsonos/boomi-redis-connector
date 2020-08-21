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

import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.util.BaseDeleteOperation;
import com.sonos.boomi.connector.redis.RedisConnection;
import com.sonos.boomi.connector.redis.util.OperationUtil;

/**
 * Responsible for all common logic used during DELETE operations
 */
public abstract class BaseRedisDeleteOperation extends BaseDeleteOperation {

    protected final RedisConnection _connection;

    /**
     * @param connection Connection provided by the Connector
     */
    protected BaseRedisDeleteOperation(RedisConnection connection) {
        super(connection);

        _connection = connection;
    }

    /**
     * Closes the Redis connection
     * @throws Throwable Unhandled exceptions
     */
    protected void finalize() throws Throwable {
        super.finalize();

        if (_connection != null) {
            _connection.closeConnection();
        }
    }

    /**
     * @return Returns the Redis connection object on this instance
     */
    protected RedisConnection getRedisConnection() {
        return _connection;
    }

    /**
     * @param deleteObjectIdData Instance of {@link com.boomi.connector.api.ObjectIdData} used to get the object Id provided on
     *                           the Boomi process connector shape
     * @return Returns the object Id to use for the provided {@link com.boomi.connector.api.ObjectIdData} instance
     */
    protected String getObjectId(ObjectIdData deleteObjectIdData) {
        return OperationUtil.getOperationObjectId(deleteObjectIdData, getContext());
    }
}
