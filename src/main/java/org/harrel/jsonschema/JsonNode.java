package org.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface JsonNode {
    String getJsonPointer();

    boolean isNull();
    boolean isBoolean();
    boolean isString();
    boolean isInteger();
    boolean isNumber();
    boolean isArray();
    boolean isObject();

    boolean asBoolean();
    String asString();
    BigInteger asInteger();
    BigDecimal asNumber();
    List<JsonNode> asArray();
    Map<String, JsonNode> asObject();

    boolean isEqualTo(JsonNode other);
}
