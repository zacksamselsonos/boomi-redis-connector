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

import com.boomi.connector.api.DeleteRequest;
import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.sonos.boomi.connector.redis.RedisConnection;
import com.sonos.boomi.connector.redis.util.StringUtil;

import java.util.*;

/**
 * Responsible for unique deletion logic when handling DELETE operations for
 * Redis HashSet data types
 */
public class RedisDeleteHashSetOperation extends BaseRedisDeleteOperation {

    private static final String RESPONSE_SUCCESS = "OK";
    private static final String RESPONSE_FAIL_ERR = "ERR";
    private static final String RESPONSE_FAIL_NOKEY = "NO_KEY";

    public RedisDeleteHashSetOperation(RedisConnection connection) {
        super(connection);
    }

    /**
     * @param deleteRequest     {@link com.boomi.connector.api.DeleteRequest} object provided by the Atom for the execution of
     *                          this DELETE operation
     * @param operationResponse Response object used to report success or failure of DELETE operation processing
     */
    @Override
    protected void executeDelete(DeleteRequest deleteRequest, OperationResponse operationResponse) {
        Set<String> keysForDeletion = new HashSet<>();
        List<ObjectIdData> keysForDeletionObjects = new ArrayList<>();
        Map<String, Set<String>> fieldsForDeletion = new HashMap<>();
        Map<String, List<ObjectIdData>> fieldsForDeletionObjects = new HashMap<>();
        try {
            // Validate and store objectIds
            for (ObjectIdData deleteObject : deleteRequest) {
                String key = getObjectId(deleteObject);
                if (StringUtil.isNullOrEmpty(key)) {
                    operationResponse.addResult(deleteObject, OperationStatus.APPLICATION_ERROR, RESPONSE_FAIL_NOKEY, "Key is a required document property", null);
                    continue;
                }

                // Get optional field value
                String field = deleteObject.getDynamicProperties().get("field");

                // Sort into operation groups
                if (StringUtil.isNullOrEmpty(field)) {
                    keysForDeletion.add(key);
                    keysForDeletionObjects.add(deleteObject);
                } else {
                    if (!fieldsForDeletion.containsKey(key)) {
                        fieldsForDeletion.put(key, new HashSet<>());
                        fieldsForDeletionObjects.put(key, new ArrayList<>());
                    }
                    fieldsForDeletion.get(key).add(field);
                    fieldsForDeletionObjects.get(key).add(deleteObject);
                }
            }

            // Delete keys from cache
            if (keysForDeletion.size() > 0) {
                Long delResult = getRedisConnection().getConnection().sync().del(keysForDeletion.toArray(new String[]{}));
                operationResponse.getLogger().fine(String.format("'DEL %s' command returned %s", com.boomi.util.StringUtil.join(" ", keysForDeletion), delResult));
                addResults(keysForDeletionObjects, operationResponse, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, null);
            }

            // Delete fields from cache
            if (fieldsForDeletion.size() > 0) {
                for (String delKey : fieldsForDeletion.keySet()) {
                    String[] fields = fieldsForDeletion.get(delKey).toArray(new String[]{});
                    Long delResult = getRedisConnection().getConnection().sync().hdel(delKey, fields);
                    operationResponse.getLogger().fine(String.format("'HDEL %s %s' command returned %s", delKey, com.boomi.util.StringUtil.join(" ", fields), delResult));
                    addResults(fieldsForDeletionObjects.get(delKey), operationResponse, OperationStatus.SUCCESS, RESPONSE_SUCCESS, null, null);
                }
            }

        } catch (Exception e) {
            addResults(keysForDeletionObjects, operationResponse, OperationStatus.FAILURE, RESPONSE_FAIL_ERR, e.getMessage(), e);
            for (String delKey : fieldsForDeletionObjects.keySet()) {
                addResults(fieldsForDeletionObjects.get(delKey), operationResponse, OperationStatus.FAILURE, RESPONSE_FAIL_ERR, e.getMessage(), e);
            }
        } finally {
            getRedisConnection().closeConnection();
        }
    }

    /**
     * @param objectDataCollection Collection containing {@link com.boomi.connector.api.ObjectIdData} instances to report result on
     * @param operationResponse    Response object used to report responses for all objects in objectKeyMap
     * @param status               {@link com.boomi.connector.api.OperationStatus} enumeration used when reporting results
     * @param statusCode           Status code used when reporting results
     * @param statusMessage        Status message used when reporting results
     * @param throwable            Exception used when reporting error results
     */
    private void addResults(Collection<ObjectIdData> objectDataCollection, OperationResponse operationResponse, OperationStatus status, String statusCode, String statusMessage, Throwable throwable) {
        for (ObjectIdData data : objectDataCollection) {
            if (status == OperationStatus.FAILURE) {
                operationResponse.addErrorResult(data, status, statusCode, statusMessage, throwable);
            } else {
                operationResponse.addEmptyResult(data, status, statusCode, statusMessage);
            }
        }
    }
}
