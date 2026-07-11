package dev.harrel.jsonschema.providers;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.internal.StandaloneNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.fasterxml.jackson.core.JsonTokenId.*;
import static dev.harrel.jsonschema.internal.InternalProviderUtil.canConvertToInteger;

// todo doc
public final class JacksonModule extends SimpleModule {

    public JacksonModule() {
        super(JacksonModule.class.getName(), Version.unknownVersion(),
                Collections.singletonMap(JsonNode.class, new Deserializer()),
                Collections.singletonList(new Serializer()));
    }

    public static final class Serializer extends JsonSerializer<JsonNode> {
        @Override
        public void serialize(JsonNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            writeNode(value, gen);
        }

        @Override
        public Class<JsonNode> handledType() {
            return JsonNode.class;
        }

        private void writeNode(JsonNode value, JsonGenerator gen) throws IOException {
            switch (value.getNodeType()) {
                case NULL:
                    gen.writeNull();
                    return;
                case BOOLEAN:
                    gen.writeBoolean(value.asBoolean());
                    return;
                case STRING:
                    gen.writeString(value.asString());
                    return;
                case INTEGER:
                    gen.writeNumber(value.asInteger());
                    return;
                case NUMBER:
                    gen.writeNumber(value.asNumber());
                    return;
                case ARRAY:
                    List<JsonNode> arr = value.asArray();
                    gen.writeStartArray(null, arr.size());
                    for (int i = 0; i < arr.size(); i++) {
                        writeNode(arr.get(i), gen);
                    }
                    gen.writeEndArray();
                    return;
                case OBJECT:
                    Map<String, JsonNode> map = value.asObject();
                    gen.writeStartObject(null, map.size());
                    for (Map.Entry<String, JsonNode> entry : map.entrySet()) {
                        gen.writeFieldName(entry.getKey());
                        writeNode(entry.getValue(), gen);
                    }
                    gen.writeEndObject();
            }
        }
    }

    public static final class Deserializer extends JsonDeserializer<JsonNode> {
        @Override
        public JsonNode deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return readNode(p, "");
        }

        /* Using deprecated API to support older versions as well */
        @Override
        @SuppressWarnings("deprecation")
        public JsonNode getNullValue() {
            return new StandaloneNode("", SimpleType.NULL, null);
        }

        private JsonNode readNode(JsonParser p, String jsonPointer) throws IOException {
            switch (p.currentTokenId()) {
                case ID_NULL:
                    return new StandaloneNode(jsonPointer, SimpleType.NULL, null);
                case ID_TRUE:
                    return new StandaloneNode(jsonPointer, SimpleType.BOOLEAN, Boolean.TRUE);
                case ID_FALSE:
                    return new StandaloneNode(jsonPointer, SimpleType.BOOLEAN, Boolean.FALSE);
                case ID_STRING:
                    return new StandaloneNode(jsonPointer, SimpleType.STRING, p.getText());
                case ID_NUMBER_INT:
                    return new StandaloneNode(jsonPointer, SimpleType.INTEGER, p.getBigIntegerValue());
                case ID_NUMBER_FLOAT:
                    return readNumber(p, jsonPointer);
                case ID_START_ARRAY:
                    return readArray(p, jsonPointer);
                case ID_START_OBJECT:
                    return readObject(p, jsonPointer);
                default:
                    throw new IllegalArgumentException("Unexpected token: " + p.currentToken().name());
            }
        }

        private JsonNode readNumber(JsonParser p, String jsonPointer) throws IOException {
            BigDecimal val = p.getDecimalValue();
            if (canConvertToInteger(val)) {
                return new StandaloneNode(jsonPointer, SimpleType.INTEGER, val.toBigInteger(), val);
            } else {
                return new StandaloneNode(jsonPointer, SimpleType.NUMBER, val);
            }
        }

        private JsonNode readArray(JsonParser p, String jsonPointer) throws IOException {
            List<JsonNode> arr = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                arr.add(readNode(p, jsonPointer + "/" + arr.size()));
            }
            return new StandaloneNode(jsonPointer, SimpleType.ARRAY, arr);
        }

        /* Using deprecated API to support older versions as well */
        @SuppressWarnings("deprecation")
        private JsonNode readObject(JsonParser p, String jsonPointer) throws IOException {
            Map<String, JsonNode> obj = new LinkedHashMap<>();
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.getCurrentName();
                p.nextToken();
                obj.put(name, readNode(p, jsonPointer + "/" + JsonNode.encodeJsonPointer(name)));
            }
            return new StandaloneNode(jsonPointer, SimpleType.OBJECT, obj);
        }
    }
}
