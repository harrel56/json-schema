package org.harrel.jsonschema.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum SimpleType {
    NULL("null"),
    BOOLEAN("boolean"),
    STRING("string"),
    INTEGER("integer"),
    NUMBER("number"),
    ARRAY("array"),
    OBJECT("object");

    private static final Map<String, SimpleType> NAME_MAP;

    static {
        Map<String, SimpleType> map = new HashMap<>();
        for (SimpleType value : SimpleType.values()) {
            map.put(value.getName(), value);
        }
        NAME_MAP = Collections.unmodifiableMap(map);
    }

    private final String name;

    SimpleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SimpleType fromName(String name) {
        return NAME_MAP.get(name);
    }
}
