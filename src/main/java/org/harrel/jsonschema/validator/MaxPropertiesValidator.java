package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MaxPropertiesValidator extends BasicValidator {
    private final int max;

    MaxPropertiesValidator(JsonNode node) {
        super("MaxProperties validation failed.");
        this.max = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return node.asObject().size() <= max;
    }
}
