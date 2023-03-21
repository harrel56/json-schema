package org.harrel.jsonschema;

interface Applicator extends Validator {

    @Override
    default ValidationResult validate(ValidationContext ctx, JsonNode node) {
        return apply(ctx, node) ? Result.success() : Result.failure();
    }

    boolean apply(ValidationContext ctx, JsonNode node);
}
