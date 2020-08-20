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

/**
 * Contains helpful methods when working with Strings
 */
public class StringUtil {
    /**
     * @param values Collection of String values to concatenate together
     * @return Returns the result of all String values being concatenated together
     */
    public static String concat(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String s : values) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * @param value Value to check
     * @return Returns whether provided value was null or empty
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value == "";
    }
}
