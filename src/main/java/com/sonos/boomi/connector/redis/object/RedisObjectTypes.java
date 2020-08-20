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

import com.boomi.connector.api.ConnectorException;
import com.boomi.connector.api.ObjectDefinitionRole;
import com.boomi.connector.api.ObjectTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Responsible for the creation of a singleton instance that stores all object types and object type definitions
 * to use and return during connector browse operations
 */
public class RedisObjectTypes {

    private final static String RUNTIME_EX = "Use getInstance() method to get the single instance of this class.";
    private final static String METADATA_RESOURCE_NAME = "connector-metadata-objecttypes.xml";
    private final static String METADATA_ELEMENT_PATH = "ObjectType";
    private final static String METADATA_SOPERATIONS_PATH = "SupportedOperations";
    private final static String METADATA_OPERATION_PATH = "Operation";

    private volatile static RedisObjectTypes singleton;

    private RedisObjectType[] _types;
    private final ObjectTypes _boomiTypes = new ObjectTypes();

    /**
     * Creates a singleton instance if one does not yet exist
     * @throws RuntimeException Thrown if a singleton instance already exists
     */
    private RedisObjectTypes() {
        if (singleton != null){
            throw new RuntimeException(RUNTIME_EX);
        }

        try {
            LoadMetadata();
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }

    /**
     * Loads the object type object graph using the metadata file from the assembly resources collection
     * @throws Exception
     */
    private void LoadMetadata() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        Document dom = domBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(METADATA_RESOURCE_NAME));

        String operationMetadataResourceFormat = dom.getDocumentElement().getAttribute("operationMetadataResourceFormat");
        NodeList elements = dom.getElementsByTagName(METADATA_ELEMENT_PATH);
        _types = new RedisObjectType[elements.getLength()];

        for (int i = 0; i < elements.getLength(); i++) {
            // Create the object type wrapper object
            Element element = (Element) elements.item(i);
            String id = element.getElementsByTagName("Id").item(0).getTextContent();
            String label = element.getElementsByTagName("Label").item(0).getTextContent();
            String helpText = element.getElementsByTagName("HelpText").item(0).getTextContent();
            RedisObjectType newType = RedisObjectTypeFactory.createInstance(id, label, helpText);

            // Create the operation metadata objects and store them on the object type wrapper
            NodeList operations = ((Element) element.getElementsByTagName(METADATA_SOPERATIONS_PATH).item(0))
                    .getElementsByTagName(METADATA_OPERATION_PATH);
            for (int j = 0; j < operations.getLength(); j++) {
                Element operation = (Element) operations.item(j);
                String type = operation.getElementsByTagName("Type").item(0).getTextContent();
                String customType = null;
                NodeList customTypeNode = operation.getElementsByTagName("CustomType");
                if (customTypeNode.getLength() > 0) {
                    customType = customTypeNode.item(0).getTextContent();
                }
                boolean hasInput = Boolean.parseBoolean(operation.getElementsByTagName("HasInput").item(0).getTextContent());
                boolean hasOutput = Boolean.parseBoolean(operation.getElementsByTagName("HasOutput").item(0).getTextContent());

                // Add input operation schema if enabled
                String inputMetdataName = operationMetadataResourceFormat
                        .replace("{id}", id)
                        .replace("{type}", type)
                        .replace("{mode}", ObjectDefinitionRole.INPUT.value())
                        .toLowerCase();
                if(hasInput && this.getClass().getClassLoader().getResource(inputMetdataName) != null) {
                    Document inputSchemaDom = domBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(inputMetdataName));
                    newType.putObjectDefinition(type, customType, ObjectDefinitionRole.INPUT, inputSchemaDom.getDocumentElement());
                } else {
                    newType.putObjectDefinition(type, customType, ObjectDefinitionRole.INPUT);
                }

                // Add output operation schema if enabled
                String outputMetadataName = operationMetadataResourceFormat
                        .replace("{id}", id)
                        .replace("{type}", type)
                        .replace("{mode}", ObjectDefinitionRole.OUTPUT.value())
                        .toLowerCase();
                if(hasOutput && this.getClass().getClassLoader().getResource(outputMetadataName) != null) {
                    Document outputSchemaDom = domBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(outputMetadataName));
                    newType.putObjectDefinition(type, customType, ObjectDefinitionRole.OUTPUT, outputSchemaDom.getDocumentElement());
                } else {
                    newType.putObjectDefinition(type, customType, ObjectDefinitionRole.OUTPUT);
                }
            }

            _types[i] = newType;
            _boomiTypes.getTypes().add(newType.toBoomiObjectType());
        }
    }

    /**
     * @return Returns a singleton instance of this class
     */
    public static RedisObjectTypes getInstance() {
        if (singleton == null) {
            synchronized (RedisObjectTypes.class) {
                if (singleton == null)
                    singleton = new RedisObjectTypes();
            }
        }

        return singleton;
    }

    /**
     * @param id Object type Id
     * @return Returns an instance of {@link RedisObjectType} if it exists in the collection
     */
    public RedisObjectType getObjectType(String id) {
        for (RedisObjectType type : _types) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * @return Returns the {@link com.boomi.connector.api.ObjectTypes} object cached on this instance
     */
    public ObjectTypes getBoomiTypes() {
        return _boomiTypes;
    }

}
