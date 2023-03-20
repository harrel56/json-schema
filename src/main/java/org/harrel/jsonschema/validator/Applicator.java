package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Result;
import org.harrel.jsonschema.ValidationContext;

interface Applicator extends Validator {

    @Override
    default ValidationResult validate(ValidationContext ctx, JsonNode node) {
        return apply(ctx, node) ? Result.success() : Result.failure();
    }

    boolean apply(ValidationContext ctx, JsonNode node);
}
