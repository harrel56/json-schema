package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
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

        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            String schemaUri = jsonPointerMap.get(entry.getKey());
            if (schemaUri != null) {
                valid = valid && ctx.resolveRequiredSchema(schemaUri).validate(ctx, entry.getValue());
            }
        }
        return valid;
    }
}
