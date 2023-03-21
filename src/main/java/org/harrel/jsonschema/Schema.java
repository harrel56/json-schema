package org.harrel.jsonschema;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Schema {

    private static final Validator TRUE_VALIDATOR = (ctx, node) -> Result.success();
    private static final Validator FALSE_VALIDATOR = (ctx, node) -> Result.failure("False schema always fails.");

    private final URI parentUri;
    private final String schemaLocation;
    private final List<ValidatorWrapper> validators;

    Schema(URI parentUri, String schemaLocation, List<ValidatorWrapper> validators) {
        this.parentUri = parentUri;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.validators = new ArrayList<>(Objects.requireNonNull(validators));
        Collections.sort(this.validators);
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
