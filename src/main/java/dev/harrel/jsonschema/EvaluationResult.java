package dev.harrel.jsonschema;

public final class EvaluationResult {
    private static final EvaluationResult SUCCESSFUL_RESULT = new EvaluationResult(true, null);
    private static final EvaluationResult FAILED_RESULT = new EvaluationResult(false, null);

    private final boolean valid;
    private final String errorMessage;

    private EvaluationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static EvaluationResult success() {
        return SUCCESSFUL_RESULT;
    }

    public static EvaluationResult failure() {
        return FAILED_RESULT;
    }

    public static EvaluationResult failure(String message) {
        return new EvaluationResult(false, message);
    }

    boolean isValid() {
        return valid;
    }

    String getErrorMessage() {
        return errorMessage;
    }
}
