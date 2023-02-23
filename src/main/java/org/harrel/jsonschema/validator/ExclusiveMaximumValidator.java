package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigDecimal;

class ExclusiveMaximumValidator extends BasicValidator {
    private final BigDecimal max;

    ExclusiveMaximumValidator(JsonNode node) {
        super("ExclusiveMaximum validation failed.");
        this.max = node.asNumber();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return true;
        }

        return node.asNumber().compareTo(max) < 0;
    }
}
