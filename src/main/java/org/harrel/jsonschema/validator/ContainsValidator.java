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
            // TODO as separate validator
            ctx.addAnnotation(new Annotation(
                    new AnnotationHeader(max.path(), null, node.getJsonPointer()), "maxContains", "MaxContains validation failed.", false));
            return false;
        }
        if (min != null) {
            if (min.value() == 0) {
                return true;
            } else if (min.value() > count) {
                // TODO as separate validator
                ctx.addAnnotation(new Annotation(
                        new AnnotationHeader(min.path(), null, node.getJsonPointer()), "minContains", "MinContains validation failed.", false));
                return false;
            }
        }
        return count > 0;
    }

    private record PathWithValue(String path, int value) {}
}
