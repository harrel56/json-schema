package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.Optional;

class RefValidator implements Validator {
    private final String ref;

    RefValidator(JsonNode node) {
        this.ref = UriUtil.decodeUrl(node.asString());
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        Optional<Schema> schema = ctx.resolveSchema(ref);
        if (schema.isEmpty()) {
            return Result.failure("Resolution of $ref (%s) failed".formatted(ref));
        } else {
            return schema.get().validate(ctx, node) ? Result.success() : Result.failure("Referenced schema validation failed.");
        }
    }
}
