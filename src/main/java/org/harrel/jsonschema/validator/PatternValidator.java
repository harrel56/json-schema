package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.regex.Pattern;

class PatternValidator extends BasicValidator {
    private final Pattern pattern;

    PatternValidator(JsonNode node) {
        super("Maximum validation failed.");
        this.pattern = Pattern.compile(node.asString());
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isString()) {
            return true;
        }

        return pattern.matcher(node.asString()).find();
    }
}
