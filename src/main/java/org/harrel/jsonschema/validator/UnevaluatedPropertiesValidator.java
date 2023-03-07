package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.Annotation;
import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class UnevaluatedPropertiesValidator extends BasicValidator {
    private final String schemaUri;
    private final String parentPath;

    UnevaluatedPropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("UnevaluatedProperties validation failed.");
        String schemaPointer = node.getJsonPointer();
        this.schemaUri = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = schemaPointer.substring(0, schemaPointer.lastIndexOf('/'));
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        List<Annotation> annotations = ctx.getAnnotations().stream()
                .filter(Annotation::successful)
                .filter(a -> a.schemaPath().startsWith(parentPath))
                .toList();
        boolean valid = true;
        for (JsonNode propertyNode : node.asObject().values()) {
            if (annotations.stream().noneMatch(a -> a.instancePath().startsWith(propertyNode.getJsonPointer()))) {
                valid = valid && ctx.resolveRequiredSchema(schemaUri).validate(ctx, propertyNode);
            }
        }
        return valid;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
