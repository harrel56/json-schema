package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

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

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        List<Annotation> annotations = ctx.getAnnotations().stream()
                .filter(a -> a.schemaPath().startsWith(parentPath))
                .toList();

        return StreamUtil.exhaustiveAllMatch(
                node.asObject()
                        .values()
                        .stream()
                        .filter(propertyNode -> annotations.stream().noneMatch(a -> a.instancePath().startsWith(propertyNode.getJsonPointer()))),
                propertyNode -> schema.validate(ctx, propertyNode)
        );
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
