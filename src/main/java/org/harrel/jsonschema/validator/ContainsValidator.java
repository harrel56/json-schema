package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.List;

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
        List<JsonNode> nodes = node.asArray();
        if (nodes.isEmpty()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        return StreamUtil.exhaustiveAnyMatch(nodes.stream(), element -> schema.validate(ctx, element));
    }
}
