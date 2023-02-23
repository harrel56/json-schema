package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigDecimal;

class ExclusiveMinimumValidator extends BasicValidator {
    private final BigDecimal min;

    ExclusiveMinimumValidator(JsonNode node) {
        super("ExclusiveMinimum validation failed.");
        this.min = node.asNumber();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return true;
        }

        return node.asNumber().compareTo(min) > 0;
    }
}
