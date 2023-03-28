package org.harrel.jsonschema;

public final class ValidationResult {
    private static final ValidationResult SUCCESSFUL_RESULT = new ValidationResult(true, null);
    private static final ValidationResult FAILED_RESULT = new ValidationResult(false, null);

    private final boolean valid;
    private final String errorMessage;

    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult success() {
        return SUCCESSFUL_RESULT;
    }

    public static ValidationResult failure() {
        return FAILED_RESULT;
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    boolean isValid() {
        return valid;
    }

    String getErrorMessage() {
        return errorMessage;
    }
}
