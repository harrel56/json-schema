package dev.harrel.jsonschema;

interface Applicator extends Validator {

    @Override
    default ValidationResult validate(ValidationContext ctx, JsonNode node) {
        return apply(ctx, node) ? ValidationResult.success() : ValidationResult.failure();
    }

    boolean apply(ValidationContext ctx, JsonNode node);
}
