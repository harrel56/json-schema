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

    private final String schemaLocation;
    private final List<ValidatorWrapper> validators;

    public Schema(String schemaLocation, List<ValidatorWrapper> validators) {
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.validators = new ArrayList<>(Objects.requireNonNull(validators));
        Collections.sort(this.validators);
    }

    public static Validator getBooleanValidator(boolean val) {
        return val ? TRUE_VALIDATOR : FALSE_VALIDATOR;
    }

    public boolean validate(ValidationContext ctx, JsonNode node) {
        int annotationsBefore = ctx.getAnnotations().size();
        boolean valid = true;
        for (ValidatorWrapper validator : validators) {
            ValidationResult result = validator.validate(ctx, node);
            Annotation annotation = new Annotation(validator.getKeywordPath(), node.getJsonPointer(), result.getErrorMessage(), result.isValid());
            ctx.addValidationAnnotation(new Annotation(schemaLocation, node.getJsonPointer(), result.getErrorMessage(), result.isValid()));
            valid = valid && result.isValid();
            ctx.addAnnotation(annotation);
        }
        if (!valid) {
            ctx.truncateAnnotationsToSize(annotationsBefore);
        }
        return valid;
    }
}
