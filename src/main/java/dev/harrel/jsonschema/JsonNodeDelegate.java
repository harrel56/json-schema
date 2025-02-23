package dev.harrel.jsonschema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

final class JsonNodeDelegate implements JsonNode {
    private final JsonNode delegate;
    private final String jsonPointer;

    JsonNodeDelegate(JsonNode delegate, String jsonPointer) {
        this.delegate = delegate;
        this.jsonPointer = jsonPointer;
    }

    JsonNode unwrap() {
        return delegate;
    }

    @Override
    public SimpleType getNodeType() {
        return delegate.getNodeType();
    }

    @Override
    public String getJsonPointer() {
        return jsonPointer;
    }

    @Override
    public boolean asBoolean() {
        return delegate.asBoolean();
    }

    @Override
    public String asString() {
        return delegate.asString();
    }

    @Override
    public BigInteger asInteger() {
        return delegate.asInteger();
    }

    @Override
    public BigDecimal asNumber() {
        return delegate.asNumber();
    }

    @Override
    public List<JsonNode> asArray() {
        return delegate.asArray();
    }

    @Override
    public Map<String, JsonNode> asObject() {
        return delegate.asObject();
    }
}
