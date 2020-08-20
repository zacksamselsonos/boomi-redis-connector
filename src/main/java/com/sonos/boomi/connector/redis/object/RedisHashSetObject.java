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

import com.boomi.connector.api.ObjectDefinitions;

/**
 * Represents a logical Redis HashSet object type during connector browsing operations
 */
public class RedisHashSetObject extends RedisObjectType {

    /**
     * @param id Id value to use when constructing the underlying {@link RedisObjectType}
     * @param label Label value to use when constructing the underlying {@link RedisObjectType}
     * @param helpText Help text to use when constructing the underlying {@link RedisObjectType}
     */
    public RedisHashSetObject(String id, String label, String helpText) {
        super(id, label, helpText);
    }

    /**
     * @param type Operation type being extended
     * @param customType Custom operation type being extended. Custom types are used when
     *                   operation type is EXECUTE
     * @param defs Object definitions being extended
     */
    @Override
    protected void extendObjectDefinitions(String type, String customType, ObjectDefinitions defs) {
    }

}
