package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

class ContainsValidator extends BasicValidator {
    private final String schemaUri;


    ContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Contains validation failed.");
        this.schemaUri = ctx.getAbsoluteUri(node);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        return StreamUtil.exhaustiveAnyMatch(node.asArray().stream(), element -> schema.validate(ctx, element));
    }
}
