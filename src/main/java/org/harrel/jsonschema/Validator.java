package org.harrel.jsonschema;

public interface Validator {
    ValidationResult validate(ValidationContext ctx, JsonNode node);
}
