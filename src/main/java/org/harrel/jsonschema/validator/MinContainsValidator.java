package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Optional;

class MinContainsValidator extends BasicValidator {
    private final int min;
    private final Optional<String> containsPath;

    MinContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("MinContains validation failed.");
        this.min = node.asInteger().intValueExact();
        this.containsPath = Optional.ofNullable(ctx.getCurrentSchemaObject().get("contains"))
                .map(JsonNode::getJsonPointer);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || containsPath.isEmpty()) {
            return true;
        }

        long count = ctx.getAnnotations().stream()
                .filter(a -> a.schemaPath().startsWith(containsPath.get()))
                .filter(a -> a.instancePath().startsWith(node.getJsonPointer()))
                .filter(a -> a.instancePath().length() != node.getJsonPointer().length())
                .filter(a -> !a.instancePath().substring(node.getJsonPointer().length()).contains("/"))
                .count();
        return Math.toIntExact(count) >= min;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
