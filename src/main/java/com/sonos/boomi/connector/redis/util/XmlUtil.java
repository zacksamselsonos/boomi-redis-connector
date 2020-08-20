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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Contains helpful methods when working with XML
 */
public class XmlUtil {

    /**
     * @param stream Stream to parse into an XML Document
     * @return Returns a new instance of {@link org.w3c.dom.Document} containing the XML from the stream
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document parseStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(stream);
    }

    /**
     * @param parent Parent element to query
     * @param tagName Tag name of child element to find
     * @return Returns the first occurance of an element under parent with a matching tag name
     */
    public static Element getElementByTagName(Element parent, String tagName) {
        if (parent == null || StringUtil.isNullOrEmpty(tagName)) {
            return null;
        }

        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }

        return (Element) nodes.item(0);
    }

    /**
     * @param parent Parent element to query
     * @param tagName Tag name of child element to find
     * @return Returns the text content of the first occurance of an element under parent
     * with a matching tag name
     */
    public static String getTextContentByTagName(Element parent, String tagName) {
        Element element = getElementByTagName(parent, tagName);
        if (element == null) {
            return null;
        }
        return element.getTextContent();
    }

}
