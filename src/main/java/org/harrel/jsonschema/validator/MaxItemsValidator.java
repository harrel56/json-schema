package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MaxItemsValidator extends BasicValidator {
    private final int maxItems;

    MaxItemsValidator(JsonNode node) {
        super("MaxItems validation failed.");
        this.maxItems = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        return node.asArray().size() <= maxItems;
    }
}
