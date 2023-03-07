package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

class NotValidator extends BasicValidator {
    private final String schemaUri;


    NotValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Not validation failed.");
        this.schemaUri = ctx.getAbsoluteUri(node);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return !ctx.resolveRequiredSchema(schemaUri).validate(ctx, node);
    }
}
