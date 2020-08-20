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
import com.sonos.boomi.connector.redis.util.StringUtil;
import com.sonos.boomi.connector.redis.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Responsible for unique getter logic when handling UPSERT operations for
 * Redis HashSet data types
 */
public class RedisUpsertHashSetOperation extends BaseRedisUpsertOperation {

    private static final String RESPONSE_SUCCESS = "OK";
    private static final String RESPONSE_FAIL_NOKEY = "NO_KEY";
    private static final String RESPONSE_FAIL_ERROR = "ERR";
    private static final String RESPONSE_FAIL_BADINPUT = "BAD_INPUT";

    /**
     * @param connection Connection provided by the Connector
     */
    public RedisUpsertHashSetOperation(RedisConnection connection) {
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
                try (InputStream inputStream = objectData.getData()) {
                    // Get inputs and validate
                    Integer ttl = getTtl(objectData);
                    String key = getKey(objectData);
                    if (key == null) {
                        operationResponse.addResult(objectData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOKEY, "Key is a required document property", null);
                        continue;
                    }

                    // Transform input into lettuce.io compatible hset command input
                    Map<String, String> data;
                    try {
                        data = getHsetInput(inputStream);
                    } catch (Exception e) {
                        operationResponse.addResult(objectData, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_BADINPUT, e.getMessage(), null);
                        continue;
                    }

                    // Upsert cache and set ttl
                    Long hsetResult = getRedisConnection().getConnection().sync().hset(key, data);
                    operationResponse.getLogger().fine(String.format("'HSET %s' command returned %s", key, hsetResult));
                    if (ttl > -1) {
                        boolean expireResult = getRedisConnection().getConnection().sync().expire(key, ttl);
                        operationResponse.getLogger().fine(String.format("'EXPIRE %s' command returned %s", key, expireResult == true ? 1 : 0));
                    }

                    // Send final response
                    try (OutputStream outputStream = getContext().createTempOutputStream()) {
                        StreamUtil.copy(inputStream, outputStream);
                        try (InputStream payloadInputStream = getContext().tempOutputStreamToInputStream(outputStream)) {
                            operationResponse.addResult(objectData, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, PayloadUtil.toPayload(payloadInputStream));
                        }
                    }
                    return;
                } catch (Exception e) {
                    operationResponse.addErrorResult(objectData, OperationStatus.FAILURE, RESPONSE_FAIL_ERROR, e.getMessage(), e);
                    return;
                }
            }
        } finally {
            getRedisConnection().closeConnection();
        }
    }

    /**
     * @param data Document data to be converted to an HSET compatible data structure
     * @return Returns a new instance to be used during HSET Redis commands
     * @throws Exception
     */
    protected Map<String, String> getHsetInput(InputStream data) throws Exception {
        Document doc = XmlUtil.parseStream(data);

        NodeList items = doc.getElementsByTagName("Item");
        HashMap<String, String> rtn = new HashMap<>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Element node = (Element) items.item(i);
            String id = XmlUtil.getTextContentByTagName(node, "ID");
            if (StringUtil.isNullOrEmpty(id)) {
                throw new Exception("ID is a required field");
            }
            String val = Objects.toString(XmlUtil.getTextContentByTagName(node, "Value"), "");
            rtn.put(id, val);
        }
        data.reset();

        return rtn;
    }

}
