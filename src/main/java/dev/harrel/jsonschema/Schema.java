package dev.harrel.jsonschema;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class Schema {

    private static final Validator TRUE_VALIDATOR = (ctx, node) -> ValidationResult.success();
    private static final Validator FALSE_VALIDATOR = (ctx, node) -> ValidationResult.failure("False schema always fails.");

    private final URI parentUri;
    private final String schemaLocation;
    private final List<ValidatorWrapper> validators;

    Schema(URI parentUri, String schemaLocation, List<ValidatorWrapper> validators) {
        this.parentUri = parentUri;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        Objects.requireNonNull(validators);
        List<ValidatorWrapper> unsortedValidators = new ArrayList<>(validators);
        Collections.sort(unsortedValidators);
        this.validators = Collections.unmodifiableList(unsortedValidators);
    }

    static Validator getBooleanValidator(boolean val) {
        return val ? TRUE_VALIDATOR : FALSE_VALIDATOR;
    }

    boolean validate(ValidationContext ctx, JsonNode node) {
        boolean outOfDynamicScope = ctx.isOutOfDynamicScope(parentUri);
        if (outOfDynamicScope) {
            ctx.pushDynamicScope(parentUri);
        }

        int annotationsBefore = ctx.getAnnotations().size();
        boolean valid = true;
        for (ValidatorWrapper validator : validators) {
            ValidationResult result = validator.validate(ctx, node);
            Annotation annotation = new Annotation(
                    new AnnotationHeader(validator.getKeywordPath(), schemaLocation, node.getJsonPointer()),
                    validator.getKeyword(), result.getErrorMessage(), result.isValid());
            ctx.addValidationAnnotation(annotation);
            ctx.addAnnotation(annotation);
            valid = valid && result.isValid();
        }
        if (!valid) {
            ctx.truncateAnnotationsToSize(annotationsBefore);
        }
        if (outOfDynamicScope) {
            ctx.popDynamicContext();
        }
        return valid;
    }
}
