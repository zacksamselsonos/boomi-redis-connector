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

package com.sonos.boomi.connector.redis.util;

import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationContext;

/**
 * Contains helpful methods used during Boomi operation execution
 */
public class OperationUtil {

    /**
     * @param getData Object data used to get the base object Id value
     * @param context Operation context used to get additional dynamic properties
     * @return Returns the final object Id to use for the provided {@link com.boomi.connector.api.ObjectIdData} instance
     */
    public static String getOperationObjectId(ObjectIdData getData, OperationContext context) {
        String objectId = getData.getObjectId();
        return getPrefixedKey(objectId, context);
    }

    /**
     * @param key Base key
     * @param context Operation context used to get any configured key prefix to use during key formatting
     * @return
     */
    public static String getPrefixedKey(String key, OperationContext context) {
        if (StringUtil.isNullOrEmpty(key)) {
            return key;
        }

        return StringUtil.concat(context.getOperationProperties().getProperty("keyPrefix"), key);
    }
}
