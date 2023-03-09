package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.List;

class UnevaluatedItemsValidator extends BasicValidator {
    private final String schemaUri;
    private final String parentPath;

    UnevaluatedItemsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("UnevaluatedItems validation failed.");
        String schemaPointer = node.getJsonPointer();
        this.schemaUri = ctx.getAbsoluteUri(schemaPointer);
        this.parentPath = schemaPointer.substring(0, schemaPointer.lastIndexOf('/'));
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        List<Annotation> annotations = ctx.getAnnotations().stream()
                .filter(a -> a.schemaPath().startsWith(parentPath))
                .toList();
        return StreamUtil.exhaustiveAllMatch(
                node.asArray()
                        .stream()
                        .filter(arrayNode -> annotations.stream().noneMatch(a -> a.instancePath().startsWith(arrayNode.getJsonPointer()))),
                arrayNode -> schema.validate(ctx, arrayNode)
        );
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
