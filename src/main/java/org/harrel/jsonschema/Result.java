package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;

import java.util.Objects;

public class Result implements ValidationResult {
    private static final Result SUCCESSFUL_RESULT = new Result(null);

    private final String errorMessage;

    private Result(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static Result success() {
        return SUCCESSFUL_RESULT;
    }

    public static Result failure(String message) {
        return new Result(Objects.requireNonNull(message));
    }

    @Override
    public boolean isValid() {
        return errorMessage == null;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
