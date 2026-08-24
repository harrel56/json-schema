package dev.harrel.jsonschema.providers;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SimpleType;
import dev.harrel.jsonschema.internal.StandaloneNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.harrel.jsonschema.internal.InternalProviderUtil.canConvertToInteger;

// todo doc
public final class GsonModule {
    private GsonModule() {}

    public static class TypeAdapter extends com.google.gson.TypeAdapter<JsonNode> {
        @Override
        public void write(JsonWriter out, JsonNode value) throws IOException {
            writeNode(out, value);
        }

        private void writeNode(JsonWriter out, JsonNode value) throws IOException {
            switch (value.getNodeType()) {
                case NULL:
                    out.nullValue();
                    return;
                case BOOLEAN:
                    out.value(value.asBoolean());
                    return;
                case STRING:
                    out.value(value.asString());
                    return;
                case INTEGER:
                    out.value(value.asInteger());
                    return;
                case NUMBER:
                    out.value(value.asNumber());
                    return;
                case ARRAY:
                    List<JsonNode> arr = value.asArray();
                    out.beginArray();
                    for (int i = 0; i < arr.size(); i++) {
                        writeNode(out, arr.get(i));
                    }
                    out.endArray();
                    return;
                case OBJECT:
                    Map<String, JsonNode> map = value.asObject();
                    out.beginObject();
                    for (Map.Entry<String, JsonNode> entry : map.entrySet()) {
                        out.name(entry.getKey());
                        writeNode(out, entry.getValue());
                    }
                    out.endObject();
            }
        }

        @Override
        public JsonNode read(JsonReader in) throws IOException {
            return readNode(in, "");
        }

        private JsonNode readNode(JsonReader in, String jsonPointer) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return new StandaloneNode(jsonPointer, SimpleType.NULL, null);
                case BOOLEAN:
                    return new StandaloneNode(jsonPointer, SimpleType.BOOLEAN, in.nextBoolean());
                case STRING:
                    return new StandaloneNode(jsonPointer, SimpleType.STRING, in.nextString());
                case NUMBER:
                    BigDecimal val = new BigDecimal(in.nextString());
                    if (canConvertToInteger(val)) {
                        return new StandaloneNode(jsonPointer, SimpleType.INTEGER, val.toBigInteger(), val);
                    } else {
                        return new StandaloneNode(jsonPointer, SimpleType.NUMBER, val);
                    }
                case BEGIN_ARRAY:
                    return readArray(in, jsonPointer);
                case BEGIN_OBJECT:
                    return readObject(in, jsonPointer);
                default:
                    throw new IllegalArgumentException("Unexpected token: " + in.peek().name());
            }
        }

        private JsonNode readArray(JsonReader in, String jsonPointer) throws IOException {
            in.beginArray();
            List<JsonNode> arr = new ArrayList<>();
            while (in.peek() != JsonToken.END_ARRAY) {
                arr.add(readNode(in, jsonPointer + "/" + arr.size()));
            }
            in.endArray();
            return new StandaloneNode(jsonPointer, SimpleType.ARRAY, arr);
        }

        private JsonNode readObject(JsonReader in, String jsonPointer) throws IOException {
            in.beginObject();
            Map<String, JsonNode> obj = new LinkedHashMap<>();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                obj.put(name, readNode(in, jsonPointer + "/" + JsonNode.encodeJsonPointer(name)));
            }
            in.endObject();
            return new StandaloneNode(jsonPointer, SimpleType.OBJECT, obj);
        }
    }
}
