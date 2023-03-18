package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.Optional;

class MaxContainsValidator extends BasicValidator {
    private final String containsPath;
    private final int max;

    MaxContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("MaxContains validation failed.");
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get("contains"))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.max = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return true;
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        return count <= max;
    }
}
