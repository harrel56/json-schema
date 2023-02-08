package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.ValidationContext;

public abstract class BasicValidator implements Validator {
    private final String errorMessage;

    protected BasicValidator(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public final ValidationResult validate(ValidationContext ctx, JsonNode node) {
        return doValidate(ctx, node) ? Result.success() : Result.failure(errorMessage);
    }

    protected abstract boolean doValidate(ValidationContext ctx, JsonNode node);
}
