package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

import java.util.List;
import java.util.Objects;

public class Schema {

    private static final Schema TRUE_SCHEMA = new Schema(List.of((ctx, node) -> Result.success()));
    private static final Schema FALSE_SCHEMA = new Schema(List.of((ctx, node) -> Result.failure("False schema always fails")));

    private final List<Validator> validators;

    public Schema(List<Validator> validators) {
        this.validators = Objects.requireNonNull(validators);
    }

    public static Schema getBooleanSchema(boolean val) {
        return val ? TRUE_SCHEMA : FALSE_SCHEMA;
    }

    public final boolean validate(ValidationContext ctx, JsonNode node) {
        ValidationContext adjustedCtx = adjustValidationContext(ctx);
        return validators.stream()
                .map(validator -> validator.validate(adjustedCtx, node))
                .allMatch(ValidationResult::isValid);
    }

    ValidationContext adjustValidationContext(ValidationContext ctx) {
        return ctx;
    }
}
