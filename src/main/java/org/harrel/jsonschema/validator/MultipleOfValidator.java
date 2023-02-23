package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigDecimal;

class MultipleOfValidator extends BasicValidator {
    private final BigDecimal factor;

    MultipleOfValidator(JsonNode node) {
        super("MultipleOf validation failed.");
        this.factor = node.asNumber();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isNumber()) {
            return true;
        }

        return node.asNumber().remainder(factor).doubleValue() == 0.0;
    }
}
