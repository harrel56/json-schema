package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.Optional;

class ContainsValidator extends BasicValidator {
    private final String schemaUri;
    private final PathWithValue max;
    private final PathWithValue min;

    ContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Contains validation failed.");
        this.schemaUri = ctx.getAbsoluteUri(node);
        this.max = Optional.ofNullable(ctx.getCurrentSchemaObject().get("maxContains"))
                .map(n -> new PathWithValue(n.getJsonPointer(), n.asInteger().intValueExact()))
                .orElse(null);
        this.min = Optional.ofNullable(ctx.getCurrentSchemaObject().get("minContains"))
                .map(n -> new PathWithValue(n.getJsonPointer(), n.asInteger().intValueExact()))
                .orElse(null);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        long count = node.asArray().stream()
                .filter(element -> schema.validate(ctx, element))
                .count();
        if (max != null && max.value() < count) {
            ctx.addAnnotation(new Annotation(max.path(), node.getJsonPointer(), "MaxContains validation failed.", false));
            return false;
        }
        if (min != null) {
            if (min.value() == 0) {
                return true;
            } else if (min.value() > count) {
                ctx.addAnnotation(new Annotation(min.path(), node.getJsonPointer(), "MinContains validation failed.", false));
                return false;
            }
        }
        return count > 0;
    }

    private record PathWithValue(String path, int value) {}
}
