package dev.harrel.jsonschema;

/**
 * {@code Keyword} class exposes keyword names in form of static final fields.
 */
public final class Keyword {

    private Keyword() {}

    /* Core */
    public static final String ID = "$id";
    public static final String SCHEMA = "$schema";
    public static final String REF = "$ref";
    public static final String ANCHOR = "$anchor";
    public static final String DYNAMIC_REF = "$dynamicRef";
    public static final String DYNAMIC_ANCHOR = "$dynamicAnchor";
    public static final String VOCABULARY = "$vocabulary";
    public static final String COMMENT = "$comment";
    public static final String DEFS = "$defs";
    public static final String DEFINITIONS = "definitions";

    /* Evaluators */
    public static final String TYPE = "type";
    public static final String CONST = "const";
    public static final String ENUM = "enum";
    public static final String MULTIPLE_OF = "multipleOf";
    public static final String MAXIMUM = "maximum";
    public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    public static final String MINIMUM = "minimum";
    public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public static final String MAX_LENGTH = "maxLength";
    public static final String MIN_LENGTH = "minLength";
    public static final String PATTERN = "pattern";
    public static final String MAX_ITEMS = "maxItems";
    public static final String MIN_ITEMS = "minItems";
    public static final String UNIQUE_ITEMS = "uniqueItems";
    public static final String MAX_CONTAINS = "maxContains";
    public static final String MIN_CONTAINS = "minContains";
    public static final String MAX_PROPERTIES = "maxProperties";
    public static final String MIN_PROPERTIES = "minProperties";
    public static final String REQUIRED = "required";
    public static final String DEPENDENT_REQUIRED = "dependentRequired";

    /* Applicators */
    public static final String PREFIX_ITEMS = "prefixItems";
    public static final String ITEMS = "items";
    public static final String CONTAINS = "contains";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String PROPERTIES = "properties";
    public static final String PATTERN_PROPERTIES = "patternProperties";
    public static final String DEPENDENT_SCHEMAS = "dependentSchemas";
    public static final String PROPERTY_NAMES = "propertyNames";
    public static final String IF = "if";
    public static final String THEN = "then";
    public static final String ELSE = "else";
    public static final String ALL_OF = "allOf";
    public static final String ANY_OF = "anyOf";
    public static final String ONE_OF = "oneOf";
    public static final String NOT = "not";
    public static final String UNEVALUATED_ITEMS = "unevaluatedItems";
    public static final String UNEVALUATED_PROPERTIES = "unevaluatedProperties";

    /* draft2019-09 */
    public static final String ADDITIONAL_ITEMS = "additionalItems";
    public static final String RECURSIVE_REF = "$recursiveRef";
    public static final String RECURSIVE_ANCHOR = "$recursiveAnchor";
}
