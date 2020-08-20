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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Contains helpful methods when working with streams
 */
public class StreamUtil {
    /**
     * @param input Input stream to copy
     * @param output Output stream to write the contents of input into
     * @throws IOException
     */
    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while((len = input.read(buf)) != -1) {
            output.write(buf, 0, len);
        }
    }

    /**
     * @param stream Input stream to convert to string
     * @param charset Charset to use during conversion
     * @return Returns a string representation of the stream object
     * @throws IOException
     */
    public static String readString(InputStream stream, Charset charset) throws IOException {
        String rtn = com.boomi.util.StreamUtil.toString(stream, charset);
        stream.reset();
        return rtn;
    }
}
