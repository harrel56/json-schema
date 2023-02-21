package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MinItemsValidator extends BasicValidator {
    private final int minItems;

    MinItemsValidator(JsonNode node) {
        super("MinItems validation failed.");
        this.minItems = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        return node.asArray().size() >= minItems;
    }
}
