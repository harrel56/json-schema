package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Optional;

class MinContainsValidator extends BasicValidator {
    private final String containsPath;
    private final int min;

    MinContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("MinContains validation failed.");
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get("contains"))
                .map(ctx::getAbsoluteUri)
                .orElse(null);
        this.min = node.asInteger().intValueExact();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath == null) {
            return true;
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.header().schemaLocation().equals(containsPath))
                .count();
        return count >= min;
    }
}
