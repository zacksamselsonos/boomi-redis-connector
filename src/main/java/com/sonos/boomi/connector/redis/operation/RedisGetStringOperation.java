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
import com.sonos.boomi.connector.redis.util.StringUtil;

/**
 * Responsible for unique getter logic when handling GET operations for
 * Redis String data types
 */
public class RedisGetStringOperation extends BaseRedisGetOperation {

    private static final String RESPONSE_SUCCESS = "OK";
    private static final String RESPONSE_FAIL_NOKEY = "NO_KEY";
    private static final String RESPONSE_FAIL_NOTFOUND = "NOT_FOUND";
    private static final String RESPONSE_FAIL_ERR = "ERR";

    /**
     * @param connection Connection provided by the Connector
     */
    public RedisGetStringOperation(RedisConnection connection) {
        super(connection);
    }

    /**
     * @param getRequest {@link com.boomi.connector.api.GetRequest} object provided by the Atom for the execution of
     *                                                             this GET operation
     * @param operationResponse Response object used to report success or failure of GET operation processing
     */
    @Override
    protected void executeGet(GetRequest getRequest, OperationResponse operationResponse) {
        ObjectIdData trackedData = getRequest.getObjectId();

        try {
            // Validate objectId
            String objectId = getObjectId(getRequest.getObjectId());
            if (StringUtil.isNullOrEmpty(objectId)) {
                operationResponse.addResult(trackedData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOKEY, "Key is a required document property", null);
                return;
            }

            // Get from cache and validate result
            String rtn = getRedisConnection().getConnection().sync().get(objectId);
            if (rtn == null) {
                boolean throwOnNotFound = getContext().getOperationProperties().getBooleanProperty("throwOnNotFound");
                if (throwOnNotFound) {
                    operationResponse.addResult(trackedData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOTFOUND, "Key not found", null);
                } else {
                    /*
                    https://help.boomi.com/bundle/connectors/page/int-Implementing_custom_connector_operations.html
                    Get — retrieves an object from the service based on an object ID. Since Get requests are commonly used to test for the existence of an object,
                    a request for an object which does not exist should not return a failure, but should instead return an "empty" success.
                     */
                    operationResponse.addEmptyResult(trackedData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null);
                }
                return;
            }

            // Get ttl
            Integer ttl = Math.toIntExact(getRedisConnection().getConnection().sync().ttl(objectId));

            // Construct metadata if there is a valid ttl
            PayloadMetadata metadata = null;
            if (ttl != -1) {
                metadata = operationResponse.createMetadata();
                metadata.setTrackedProperty("ttl", String.valueOf((ttl == -2 ? 0 : ttl)));
            }

            // Send final payload response
            if (metadata != null) {
                operationResponse.addResult(trackedData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(rtn, metadata));
            } else {
                operationResponse.addResult(trackedData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(rtn));
            }
            return;
        } catch (Exception e) {
            operationResponse.addErrorResult(trackedData, OperationStatus.FAILURE, RESPONSE_FAIL_ERR, e.getMessage(), e);
            return;
        } finally {
            getRedisConnection().closeConnection();
        }
    }

}
