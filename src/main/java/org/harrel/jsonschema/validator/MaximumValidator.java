package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigDecimal;

class MaximumValidator extends BasicValidator {
    private final BigDecimal max;

    MaximumValidator(JsonNode node) {
        super("Maximum validation failed.");
        this.max = node.asNumber();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return true;
        }

        return node.asNumber().compareTo(max) <= 0;
    }
}
