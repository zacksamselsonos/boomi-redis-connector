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

import com.boomi.connector.api.*;
import com.sonos.boomi.connector.redis.RedisConnection;
import com.sonos.boomi.connector.redis.util.StreamUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Responsible for unique getter logic when handling UPSERT operations for
 * Redis HashSet data types
 */
public class RedisUpsertStringOperation extends BaseRedisUpsertOperation {

    private static final String RESULT_SET_SUCCESS = "OK";
    private static final String RESULT_SETEX_SUCCESS = "OK";

    private static final String RESPONSE_SUCCESS = "OK";
    private static final String RESPONSE_FAIL_NOKEY = "NO_KEY";
    private static final String RESPONSE_FAIL_ERROR = "ERR";

    /**
     * @param connection Connection provided by the Connector
     */
    public RedisUpsertStringOperation(RedisConnection connection) {
        super(connection);
    }

    /**
     * @param updateRequest {@link com.boomi.connector.api.UpdateRequest} object provided by the Atom for the execution of
     *                                                             this UPSERT operation
     * @param operationResponse Response object used to report success or failure of UPSERT operation processing
     */
    @Override
    protected void executeUpdate(UpdateRequest updateRequest, OperationResponse operationResponse) {
        try {
            for (ObjectData objectData : updateRequest) {
                try (InputStream inputStream = objectData.getData())
                {
                    // Get inputs and validate
                    Integer ttl = getTtl(objectData);
                    String key = getKey(objectData);
                    if (key == null) {
                        operationResponse.addResult(objectData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOKEY, "Key is a required document property", null);
                        continue;
                    }

                    // Upsert cache and set ttl
                    boolean success = false;
                    String result = null;
                    String data = StreamUtil.readString(inputStream, StandardCharsets.UTF_8);
                    if (ttl > -1) {
                        result = getRedisConnection().getConnection().sync().setex(key, ttl, data);
                        success = result == RESULT_SETEX_SUCCESS;
                    } else {
                        result = getRedisConnection().getConnection().sync().set(key, data);
                        success = result == RESULT_SET_SUCCESS;
                    }
                    operationResponse.getLogger().fine(String.format("'SETEX %s' command returned %s", key, result));

                    // Send final payload response
                    if (success) {
                        try (OutputStream outputStream = getContext().createTempOutputStream()) {
                            StreamUtil.copy(inputStream, outputStream);
                            try (InputStream payloadInputStream = getContext().tempOutputStreamToInputStream(outputStream)) {
                                operationResponse.addResult(objectData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(payloadInputStream));
                            }
                        }
                        return;
                    } else {
                        operationResponse.addResult(objectData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_ERROR, result, ResponseUtil.toPayload(data));
                    }
                } catch (Exception e) {
                    operationResponse.addErrorResult(objectData, OperationStatus.FAILURE, RESPONSE_FAIL_ERROR, e.getMessage(), e);
                }
            }
        } finally {
            getRedisConnection().closeConnection();
        }
    }

}
