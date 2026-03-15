package dev.harrel.json.providers.jackson3;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.internal.GenericNode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Deser extends ValueDeserializer<JsonNode> {
    @Override
    public JsonNode deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        return readNode(p, "");
    }

    @Override
    public JsonNode getNullValue(DeserializationContext ctx) {
        return new GenericNode("", SimpleType.NULL, null);
    }

    private JsonNode readNode(JsonParser p, String jsonPointer) {
        return switch (p.currentToken()) {
            case VALUE_NULL -> new GenericNode(jsonPointer, SimpleType.NULL, null);
            case VALUE_TRUE, VALUE_FALSE -> new GenericNode(jsonPointer, SimpleType.BOOLEAN, p.getBooleanValue());
            case VALUE_STRING -> new GenericNode(jsonPointer, SimpleType.STRING, p.getString());
            case VALUE_NUMBER_INT -> new GenericNode(jsonPointer, SimpleType.INTEGER, p.getBigIntegerValue());
            case VALUE_NUMBER_FLOAT -> readNumber(p, jsonPointer);
            case START_ARRAY -> readArray(p, jsonPointer);
            case START_OBJECT -> readObject(p, jsonPointer);
            case NOT_AVAILABLE,
                 END_OBJECT,
                 END_ARRAY,
                 PROPERTY_NAME,
                 VALUE_EMBEDDED_OBJECT -> throw new UnsupportedOperationException(p.currentToken().name()); // todo better msg
        };
    }

    private JsonNode readNumber(JsonParser p, String jsonPointer) {
        BigDecimal val = p.getDecimalValue();
        // todo reuse
        if (val.scale() <= 0 || val.stripTrailingZeros().scale() <= 0) {
            return new GenericNode(jsonPointer, SimpleType.INTEGER, val.toBigInteger());
        } else {
            return new GenericNode(jsonPointer, SimpleType.NUMBER, val);
        }
    }

    private JsonNode readArray(JsonParser p, String jsonPointer) {
        List<JsonNode> arr = new ArrayList<>();
        while (p.nextToken() != JsonToken.END_ARRAY) {
            arr.add(readNode(p, jsonPointer + "/" + arr.size()));
        }
        return new GenericNode(jsonPointer, SimpleType.ARRAY, arr);
    }

    private JsonNode readObject(JsonParser p, String jsonPointer) {
        Map<String, JsonNode> obj = new LinkedHashMap<>();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            String name = p.currentName();
            p.nextToken();
            obj.put(name, readNode(p, jsonPointer + "/" + JsonNode.encodeJsonPointer(name)));
        }
        return new GenericNode(jsonPointer, SimpleType.OBJECT, obj);
    }
}
