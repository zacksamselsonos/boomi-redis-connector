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

import com.boomi.connector.api.*;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Base Redis object used by all logical object type classes during connector browse operations.
 *
 * The primary purpose of all implementations of this abstract class to provide individual class
 * definitions whose responsibility is encapsulating all unique logic having to do with each
 * data type supported by Redis.
 */
public abstract class RedisObjectType {

    private final static String OBJECT_DEFINITION_NAME_FORMAT = "{type}_{mode}";

    protected final String _id;
    protected final String _label;
    protected final String _helpText;
    protected HashMap<String, ObjectDefinition> _objectDefinitionList = new HashMap<>();

    /**
     * @param id Id of the object type
     * @param label Label of the object type
     * @param helpText Help text of the object type
     */
    protected RedisObjectType(String id, String label, String helpText) {
        _id = id;
        _label = label;
        _helpText = helpText;
    }

    /**
     * @param type Operation type used during hash key construction
     * @param customType Custom operation type used during hash key construction
     * @param role {@link com.boomi.connector.api.ObjectDefinitionRole} used during hash key construction.
     *                                                                 Provided by connector Browse operation
     * @return Returns the unique hash key to use during object definition lookups
     */
    protected String getObjectDefinitionHashKey(String type, String customType, ObjectDefinitionRole role){
        return OBJECT_DEFINITION_NAME_FORMAT
                .replace("{type}", type + customType)
                .replace("{mode}", role.value())
                .toLowerCase();
    }

    /**
     * @param type Operation type used during hash key and object construction
     * @param customType Custom operation type used during hash key and object construction
     * @param role {@link com.boomi.connector.api.ObjectDefinitionRole} used during hash key construction.
     *                                                                 Provided by connector Browse operation
     */
    protected void putObjectDefinition(String type, String customType, ObjectDefinitionRole role) {
        putObjectDefinition(type, customType, role, null);
    }

    /**
     * @param type Operation type used during hash key and object construction
     * @param customType Custom operation type used during hash key and object construction
     * @param role {@link com.boomi.connector.api.ObjectDefinitionRole} used during hash key and object construction.
     *                                                                 Provided by connector Browse operation
     * @param schema Schema to store on the object definition being constructed
     */
    protected void putObjectDefinition(String type, String customType, ObjectDefinitionRole role, Element schema) {
        ObjectDefinition newDef = new ObjectDefinition();
        if (schema != null) {
            newDef.setSchema(schema);
            if (role == ObjectDefinitionRole.INPUT) {
                newDef.setInputType(ContentType.XML);
            } else if (role == ObjectDefinitionRole.OUTPUT) {
                newDef.setOutputType(ContentType.XML);
            }
        } else {
            if (role == ObjectDefinitionRole.INPUT) {
                newDef.setInputType(ContentType.BINARY);
            } else if (role == ObjectDefinitionRole.OUTPUT) {
                newDef.setOutputType(ContentType.BINARY);
            }
        }

        _objectDefinitionList.put(getObjectDefinitionHashKey(type, customType, role), newDef);
    }

    /**
     * @param type Operation type used during hash key and object construction
     * @param customType Custom operation type used during hash key and object construction
     * @param role {@link com.boomi.connector.api.ObjectDefinitionRole} used during hash key and object construction.
     *                                                                 Provided by connector Browse operation
     * @return Returns an instance of {@link com.boomi.connector.api.ObjectDefinition} from the object definition map
     * if one exists. Returns null if it doesn't exist.
     */
    protected ObjectDefinition getObjectDefinition(String type, String customType, ObjectDefinitionRole role) {
        return _objectDefinitionList.get(getObjectDefinitionHashKey(type, customType, role));
    }

    /**
     * @param type Operation type being extended
     * @param customType Custom operation type being extended. Custom types are used when
     *                   operation type is EXECUTE
     * @param defs Object definitions being extended
     */
    protected abstract void extendObjectDefinitions(String type, String customType, ObjectDefinitions defs);

    /**
     * @return Returns a new instance of {@link com.boomi.connector.api.ObjectType} using the data contained in
     * this instance
     */
    public ObjectType toBoomiObjectType() {
        ObjectType rtn = new ObjectType();
        rtn.setId(getId());
        rtn.setLabel(getLabel());
        rtn.setHelpText(getHelpText());
        return rtn;
    }

    /**
     * @param type Operation type used to lookup the correct object definition
     * @param customType Custom operation type used to lookup the correct object definition
     * @param roles Collection of {@link com.boomi.connector.api.ObjectDefinitionRole} used to lookup the
     *              correct object definition
     * @return Returns a new instance of {@link com.boomi.connector.api.ObjectDefinitions} containing all
     * matching data stored on this object
     */
    public ObjectDefinitions toObjectDefinitions(String type, String customType, Collection<ObjectDefinitionRole> roles) {
        ObjectDefinitions rtn = new ObjectDefinitions();
        List<ObjectDefinition> defs = rtn.getDefinitions();
        for (ObjectDefinitionRole role : roles) {
            ObjectDefinition def = getObjectDefinition(type, customType, role);
            if (def == null) throw new IndexOutOfBoundsException(
                    String.format("Could not find an object definition id:%s type:%s mode:%s", _id, type, role.value())
            );
            defs.add(def);
        }
        extendObjectDefinitions(type, customType, rtn);
        return rtn;
    }

    public String getId() {
        return _id;
    }

    public String getLabel() {
        return _label;
    }

    public String getHelpText() {
        return _helpText;
    }
}
