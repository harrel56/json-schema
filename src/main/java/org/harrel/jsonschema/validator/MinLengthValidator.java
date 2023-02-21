package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class MinLengthValidator extends BasicValidator {
    private final int minLength;

    MinLengthValidator(JsonNode node) {
        super("MinLength validation failed.");
        this.minLength = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return true;
        }

        String string = node.asString();
        return string.codePointCount(0, string.length()) >= minLength;
    }
}
