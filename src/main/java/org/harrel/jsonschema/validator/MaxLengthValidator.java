package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MaxLengthValidator extends BasicValidator {
    private final int maxLength;

    MaxLengthValidator(JsonNode node) {
        super("MaxLength validation failed.");
        this.maxLength = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return true;
        }

        String string = node.asString();
        return string.codePointCount(0, string.length()) <= maxLength;
    }
}
