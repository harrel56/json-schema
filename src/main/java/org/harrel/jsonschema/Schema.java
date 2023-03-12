package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

import java.util.*;

public class Schema {

    private static final Validator TRUE_VALIDATOR = (ctx, node) -> Result.success();
    private static final Validator FALSE_VALIDATOR = (ctx, node) -> Result.failure("False schema always fails.");

    private final List<ValidatorDelegate> validators;

    public Schema(List<ValidatorDelegate> validators) {
        this.validators = new ArrayList<>(Objects.requireNonNull(validators));
        Collections.sort(this.validators);
    }

    public static Validator getBooleanValidator(boolean val) {
        return val ? TRUE_VALIDATOR : FALSE_VALIDATOR;
    }

    public boolean validate(ValidationContext ctx, JsonNode node) {
        ValidationContext newCtx = ctx.withEmptyAnnotations();
        for (ValidatorDelegate validator : validators) {
            ValidationResult result = validator.validate(newCtx, node);
            if (!result.isValid()) {
                return false;
            }
            newCtx.addAnnotation(new Annotation(validator.getKeywordPath(), node.getJsonPointer(), result.getErrorMessage(), result.isValid()));
        }
        ctx.addAnnotations(newCtx.getAnnotations());
        return true;
    }
}
