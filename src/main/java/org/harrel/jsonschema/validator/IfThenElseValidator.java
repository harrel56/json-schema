package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Optional;

class IfThenElseValidator extends BasicValidator {
    private final String ifPointer;
    private final Optional<String> thenPointer;
    private final Optional<String> elsePointer;

    IfThenElseValidator(SchemaParsingContext ctx, JsonNode node) {
        super("If validation failed.");
        this.ifPointer = ctx.getAbsoluteUri(node);
        this.thenPointer = Optional.ofNullable(ctx.getCurrentSchemaObject().get("then"))
                .map(ctx::getAbsoluteUri);
        this.elsePointer = Optional.ofNullable(ctx.getCurrentSchemaObject().get("else"))
                .map(ctx::getAbsoluteUri);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (ctx.resolveRequiredSchema(ifPointer).validate(ctx, node)) {
            return thenPointer
                    .map(ctx::resolveRequiredSchema)
                    .map(schema -> schema.validate(ctx, node)).orElse(true);
        } else {
            return elsePointer
                    .map(ctx::resolveRequiredSchema)
                    .map(schema -> schema.validate(ctx, node)).orElse(true);
        }
    }
}
