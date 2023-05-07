package dev.harrel.jsonschema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code SimpleType} enum represent all types allowed in JSON schema specification.
 */
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

    /**
     * Name getter.
     * @return name of type compatible with JSON schema specification
     */
    public String getName() {
        return name;
    }

    /**
     * Helper method for getting {@code SimpleType} from JSON schema types.
     * @param name name of type compatible with JSON schema specification
     * @return corresponding {@code SimpleType}, null if not found
     */
    public static SimpleType fromName(String name) {
        return NAME_MAP.get(name);
    }
}
