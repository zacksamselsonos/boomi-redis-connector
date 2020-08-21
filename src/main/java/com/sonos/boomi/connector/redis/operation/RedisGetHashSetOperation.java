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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for unique getter logic when handling GET operations for
 * Redis HashSet data types
 */
public class RedisGetHashSetOperation extends BaseRedisGetOperation {

    private static final String RESPONSE_SUCCESS = "OK";
    private static final String RESPONSE_FAIL_NOTFOUND = "NOT_FOUND";
    private static final String RESPONSE_FAIL_ERR = "ERR";

    /**
     * @param connection Connection provided by the Connector
     */
    public RedisGetHashSetOperation(RedisConnection connection) {
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
                operationResponse.addResult(trackedData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOTFOUND, "Key is a required document property", null);
                return;
            }

            // Get optional field value
            String field = trackedData.getDynamicProperties().get("field");

            // Get from cache and validate result
            Map<String, String> rtn;
            if (StringUtil.isNullOrEmpty(field)) {
                rtn = getRedisConnection().getConnection().sync().hgetall(objectId);
            } else {
                String fieldRtn = getRedisConnection().getConnection().sync().hget(objectId, field);
                rtn = new HashMap<>(1);
                if (fieldRtn != null) {
                    rtn.put(field, fieldRtn);
                }
            }
            if (rtn.size() == 0) {
                boolean throwOnNotFound = getContext().getOperationProperties().getBooleanProperty("throwOnNotFound");
                if (throwOnNotFound) {
                    String keyNotFoundFormat = "Key %s not found", keyFieldNotFound = "Key %s / field %s not found";
                    operationResponse.addResult(trackedData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOTFOUND, String.format(StringUtil.isNullOrEmpty(field) ? keyNotFoundFormat : keyFieldNotFound, objectId, field), null);
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
            int ttl = Math.toIntExact(getRedisConnection().getConnection().sync().ttl(objectId));

            // Construct metadata if there is a valid ttl
            PayloadMetadata metadata = null;
            if (ttl != -1) {
                metadata = operationResponse.createMetadata();
                metadata.setTrackedProperty("ttl", String.valueOf((ttl == -2 ? 0 : ttl)));
            }

            // Send final payload response
            try (OutputStream outputStream = mapToGetResult(getContext(), rtn);
                 InputStream payloadInputStream = getContext().tempOutputStreamToInputStream(outputStream)) {
                if (metadata != null) {
                    operationResponse.addResult(trackedData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(payloadInputStream, metadata));
                } else {
                    operationResponse.addResult(trackedData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(payloadInputStream));
                }
            }
        } catch (Exception e) {
            operationResponse.addErrorResult(trackedData, OperationStatus.FAILURE, RESPONSE_FAIL_ERR, e.getMessage(), e);
        } finally {
            getRedisConnection().closeConnection();
        }
    }

    /**
     * @param context Operation context used to create temporary output streams for memory management purposes
     * @param values Map of ID/Value pairs to write into an XML fragment
     * @return Returns an {@link java.io.OutputStream} containing the XML output of the GET operation
     * @throws IOException Throws on IO exception
     */
    private OutputStream mapToGetResult(OperationContext context, Map<String, String> values) throws IOException {
        OutputStream getResult = context.createTempOutputStream();
        getResult.write("<HashSet>".getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, String> item : values.entrySet()) {
            getResult.write(String.format("<Item><ID>%s</ID><Value>%s</Value></Item>", item.getKey(), item.getValue()).getBytes(StandardCharsets.UTF_8));
        }
        getResult.write("</HashSet>".getBytes(StandardCharsets.UTF_8));
        getResult.flush();
        return getResult;
    }

}
