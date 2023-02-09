package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MinPropertiesValidator extends BasicValidator {
    private final int min;

    MinPropertiesValidator(JsonNode node) {
        super("MinProperties validation failed.");
        this.min = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return node.asObject().size() >= min;
    }
}
