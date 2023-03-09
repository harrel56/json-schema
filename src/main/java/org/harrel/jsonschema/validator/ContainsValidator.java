package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

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
        return node.asArray().stream().reduce(Boolean.FALSE, (valid, element) -> schema.validate(ctx, element) || valid, (b1, b2) -> b1 || b2);
    }
}
