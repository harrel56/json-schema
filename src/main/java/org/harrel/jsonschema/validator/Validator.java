package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

public interface Validator {
    ValidationResult validate(ValidationContext ctx, JsonNode node);
}
