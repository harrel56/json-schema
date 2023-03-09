package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Optional;

class MaxContainsValidator extends BasicValidator {
    private final int max;
    private final Optional<String> containsPath;

    MaxContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("MaxContains validation failed.");
        this.max = node.asInteger().intValueExact();
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
        return Math.toIntExact(count) <= max;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
