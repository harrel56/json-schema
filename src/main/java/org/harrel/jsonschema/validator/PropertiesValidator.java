package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.StreamUtil;
import org.harrel.jsonschema.ValidationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class PropertiesValidator extends BasicValidator {
    private final Map<String, String> jsonPointerMap;

    PropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Properties validation failed.");
        Map<String, String> uris = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            uris.put(entry.getKey(), ctx.getAbsoluteUri(entry.getValue()));
        }
        this.jsonPointerMap = Collections.unmodifiableMap(uris);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return StreamUtil.exhaustiveAllMatch(
                node.asObject()
                        .entrySet()
                        .stream()
                        .filter(e -> jsonPointerMap.containsKey(e.getKey()))
                        .map(e -> Map.entry(jsonPointerMap.get(e.getKey()), e.getValue())),
                e -> ctx.resolveRequiredSchema(e.getKey()).validate(ctx, e.getValue())
        );
    }
}
