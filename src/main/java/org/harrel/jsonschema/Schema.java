package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        List<Annotation> annotations = new ArrayList<>(validators.size());
        ValidationContext newCtx = ctx.withEmptyAnnotations();
        for (ValidatorDelegate validator : validators) {
            ValidationResult result = validator.validate(newCtx, node);
            if (!result.isValid()) {
                return false;
            }
            annotations.add(new Annotation(validator.getKeywordPath(), node.getJsonPointer(), result.getErrorMessage(), result.isValid()));
        }
        ctx.addAnnotations(annotations);
        ctx.addAnnotations(newCtx.getAnnotations());
        return true;
    }
}
