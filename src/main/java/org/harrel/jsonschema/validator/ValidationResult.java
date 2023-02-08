package org.harrel.jsonschema.validator;

public interface ValidationResult {
    boolean isValid();
    String getErrorMessage();
}
