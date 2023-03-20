package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;

public class Result implements ValidationResult {
    private static final Result SUCCESSFUL_RESULT = new Result(true, null);
    private static final Result FAILED_RESULT = new Result(false, null);

    private final boolean valid;
    private final String errorMessage;

    private Result(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static Result success() {
        return SUCCESSFUL_RESULT;
    }
    public static Result failure() {
        return FAILED_RESULT;
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
