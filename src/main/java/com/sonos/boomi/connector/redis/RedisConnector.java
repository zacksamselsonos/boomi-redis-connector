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
import com.boomi.connector.util.BaseConnector;
import com.sonos.boomi.connector.redis.logging.ContainerLogger;
import com.sonos.boomi.connector.redis.operation.*;
import io.lettuce.core.RedisClient;

/**
 * Implements BaseConnector and acts as an operation factory for the Boomi atom
 */
public class RedisConnector extends BaseConnector {

    private RedisClient _redisClient;
    private boolean _disposed = false;

    /**
     * Instantiates a new instance of RedisConnector and creates the RedisClient object
     * used by all operation connections
     */
    public RedisConnector() {
        super();

        ensureRedisClient();
    }

    /**
     * Ensures all Redis connections and the Redis client are closed
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (_disposed) {
            return;
        }

        synchronized (RedisConnector.class) {
            if (_disposed) {
                return;
            }

            super.finalize();

            if (_redisClient != null) {
                _redisClient.shutdown();
            }

            _disposed = true;
        }
    }

    /**
     * Ensures that a single RedisClient object is created
     */
    private void ensureRedisClient() {
        if (_redisClient != null) {
            return;
        }

        synchronized (RedisConnector.class) {
            if (_redisClient != null) {
                return;
            }

            ContainerLogger.getInstance().info("Creating shared lettuce.io RedisClient");
            _redisClient = RedisClient.create();
        }
    }

    /**
     * @return Returns the RedisClient object
     */
    public RedisClient getRedisClient() {
        return _redisClient;
    }

    /**
     * @param browseContext Context of the browse operation provided by the Boomi atom
     * @return Returns a new instance of Browse to be used by atom browse logic
     */
    @Override
    public Browser createBrowser(BrowseContext browseContext) {
        return new RedisBrowser(new RedisConnection(browseContext));
    }

    /**
     * @param context Context of the get operation provided by the Boomi atom
     * @return Returns a new instance of Operation to be used by atom get logic
     */
    @Override
    protected Operation createGetOperation(OperationContext context) {
        String objectType = context.getObjectTypeId();
        switch (objectType) {
            case "String":
                return new RedisGetStringOperation(new RedisConnection(context));
            case "HashSet":
                return new RedisGetHashSetOperation(new RedisConnection(context));
            default:
                throw new ConnectorException("Get operation for " + objectType + " objects is not implemented");
        }
    }

    /**
     * @param context Context of the upsert operation provided by the Boomi atom
     * @return Returns a new instance of Operation to be used by atom upsert logic
     */
    @Override
    protected Operation createUpsertOperation(OperationContext context) {
        String objectType = context.getObjectTypeId();
        switch (objectType) {
            case "String":
                return new RedisUpsertStringOperation(new RedisConnection(context));
            case "HashSet":
                return new RedisUpsertHashSetOperation(new RedisConnection(context));
            default:
                throw new ConnectorException("Upsert operation for " + objectType + " objects is not implemented");
        }
    }

    /**
     * @param context Context of the delete operation provided by the Boomi atom
     * @return Returns a new instance of Operation to be used by atom delete logic
     */
    @Override
    protected Operation createDeleteOperation(OperationContext context) {
        String objectType = context.getObjectTypeId();
        switch (objectType) {
            case "String":
                return new RedisDeleteStringOperation(new RedisConnection(context));
            case "HashSet":
                return new RedisDeleteHashSetOperation(new RedisConnection(context));
            default:
                throw new ConnectorException("Delete operation for " + objectType + " objects is not implemented");
        }
    }
}
