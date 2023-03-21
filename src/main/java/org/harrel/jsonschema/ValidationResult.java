package org.harrel.jsonschema;

public interface ValidationResult {
    boolean isValid();
    String getErrorMessage();
}
