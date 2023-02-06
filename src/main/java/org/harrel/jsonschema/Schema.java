package org.harrel.jsonschema;

import java.util.List;
import java.util.Objects;

public class Schema implements Validator {

    private static final Schema TRUE_SCHEMA = new Schema(List.of((ctx, node) -> Result.success()));
    private static final Schema FALSE_SCHEMA = new Schema(List.of((ctx, node) -> Result.failure("False schema always fails")));

    private final List<Validator> validators;

    public Schema(List<Validator> validators) {
        this.validators = Objects.requireNonNull(validators);
    }

    public static Schema getBooleanSchema(boolean val) {
        return val ? TRUE_SCHEMA : FALSE_SCHEMA;
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        boolean valid = validators.stream()
                .map(validator -> validator.validate(ctx, node))
                .allMatch(ValidationResult::isValid);
        return valid ? Result.success() : Result.failure("Schema validation failed.");
    }
}
