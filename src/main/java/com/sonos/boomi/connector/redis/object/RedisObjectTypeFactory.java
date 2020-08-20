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

package com.sonos.boomi.connector.redis.object;

/**
 * Class responsible for the creation of {@link RedisObjectType} instances
 */
public class RedisObjectTypeFactory {

    /**
     * @param id Object type ID. Used to determine the type of object to create
     * @param label Label to use when constructing the new object
     * @param helpText Help text to use when constructing the new object
     * @return Returns a new instance of {@link RedisObjectType}
     * @throws Exception Thrown when Id value is not a supported object type
     */
    public static RedisObjectType createInstance(String id, String label, String helpText) throws Exception {
        switch (id) {
            case "String":
                return new RedisStringObject(id, label, helpText);
            case "HashSet":
                return new RedisHashSetObject(id, label, helpText);
            default:
                throw new Exception("Object type " + id + " is not supported");
        }
    }
}
