package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

import java.util.List;
import java.util.Objects;

public class Schema {

    private static final Validator TRUE_VALIDATOR = (ctx, node) -> Result.success();
    private static final Validator FALSE_VALIDATOR = (ctx, node) -> Result.failure("False schema always fails");

    private final List<Validator> validators;

    public Schema(List<Validator> validators) {
        this.validators = Objects.requireNonNull(validators);
    }

    public static Validator getBooleanValidator(boolean val) {
        return val ? TRUE_VALIDATOR : FALSE_VALIDATOR;
    }

    public boolean validate(ValidationContext ctx, JsonNode node) {
        return validators.stream()
                .map(validator -> validator.validate(ctx, node))
                .allMatch(ValidationResult::isValid);
    }
}
