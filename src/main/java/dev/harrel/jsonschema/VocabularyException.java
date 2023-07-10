package dev.harrel.jsonschema;

/**
 * Exception type used to indicate vocabulary related issues.
 */
public class VocabularyException extends JsonSchemaException {
    VocabularyException(String message) {
        super(message);
    }
}
