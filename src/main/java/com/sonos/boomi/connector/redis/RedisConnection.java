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

import com.boomi.connector.api.ConnectorContext;
import com.boomi.connector.util.BaseConnection;
import com.boomi.util.StringUtil;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BaseConnection that provides common connection logic to all operations
 */
public class RedisConnection extends BaseConnection {

    private final Object synch = new Object();
    private StatefulRedisConnection<String, String> _connection;
    private boolean _disposed = false;

    /**
     * @param context Context to load into the connection
     */
    public RedisConnection(ConnectorContext context) {
        super(context);
    }

    /**
     * Ensures proper cleanup of open Redis connections
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (_disposed) {
            return;
        }

        synchronized (synch) {
            if (_disposed) {
                return;
            }

            super.finalize();

            if (_connection != null) {
                _connection.close();
            }

            _disposed = true;
        }
    }

    /**
     * @return Returns a singleton instance of a Redis connection
     */
    public StatefulRedisConnection<String, String> getConnection() {
        if (_connection != null) {
            return _connection;
        }
        synchronized (synch) {
            if (_connection != null) {
                return _connection;
            }

            String[] hosts = getContext().getConnectionProperties().getProperty("hosts").split(";");
            List<RedisURI> nodes = Arrays.stream(hosts)
                    .filter(host -> StringUtil.isNotBlank(host))
                    .map(host -> RedisURI.create(host))
                    .collect(Collectors.toList());
            StatefulRedisMasterReplicaConnection<String, String> connection = MasterReplica.connect(((RedisConnector) getConnector()).getRedisClient(), StringCodec.UTF8, nodes);
            connection.setReadFrom(ReadFrom.UPSTREAM_PREFERRED);
            _connection = connection;
        }
        return _connection;
    }

    /**
     * Closes the Redis connection
     */
    public void closeConnection() {
        if (_connection == null) {
            return;
        }

        synchronized (synch) {
            if (_connection == null) {
                return;
            }

            _connection.close();
            _connection = null;
        }
    }

}
