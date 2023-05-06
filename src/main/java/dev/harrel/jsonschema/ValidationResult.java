package dev.harrel.jsonschema;

import java.util.List;
import java.util.Objects;

public final class ValidationResult {
    private final boolean valid;
    private final List<Annotation> annotations;
    private final List<Annotation> validationAnnotations;

    ValidationResult(boolean valid, List<Annotation> annotations, List<Annotation> validationAnnotations) {
        this.valid = valid;
        this.annotations = Objects.requireNonNull(annotations);
        this.validationAnnotations = Objects.requireNonNull(validationAnnotations);
    }

    static ValidationResult fromEvaluationContext(boolean valid, EvaluationContext ctx) {
        return new ValidationResult(valid, List.copyOf(ctx.getAnnotations()), List.copyOf(ctx.getValidationAnnotations()));
    }

    public boolean isValid() {
        return valid;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<Annotation> getValidationAnnotations() {
        return validationAnnotations;
    }
}
