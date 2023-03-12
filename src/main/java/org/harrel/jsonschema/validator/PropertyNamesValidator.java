package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

class PropertyNamesValidator extends BasicValidator {
    private final String schemaUri;

    PropertyNamesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("PropertyNames validation failed.");
        this.schemaUri = ctx.getAbsoluteUri(node);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        return StreamUtil.exhaustiveAllMatch(
                node.asObject().keySet().stream(),
                propName -> schema.validate(ctx, new StringNode(propName, node.getJsonPointer()))
        );
    }
}
