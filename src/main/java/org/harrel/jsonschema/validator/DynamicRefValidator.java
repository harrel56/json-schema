package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.ValidationContext;

import java.util.Optional;

class DynamicRefValidator implements Validator {
    private final String ref;

    DynamicRefValidator(JsonNode node) {
        this.ref = node.asString();
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        Optional<Schema> schema = ctx.resolveDynamicSchema(ref);
        if (schema.isEmpty()) {
            return Result.failure("Resolution of $ref (%s) failed".formatted(ref));
        } else {
            return schema.get().validate(ctx, node) ? Result.success() : Result.failure("Referenced schema validation failed.");
        }
    }
}
