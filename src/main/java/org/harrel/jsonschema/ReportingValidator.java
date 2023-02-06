package org.harrel.jsonschema;

class ReportingValidator implements Validator {
    private final SchemaParsingContext ctx;
    private final JsonNode node;
    private final Validator validator;

    public ReportingValidator(SchemaParsingContext ctx, JsonNode node, Validator validator) {
        this.ctx = ctx;
        this.node = node;
        this.validator = validator;
    }

    public ValidationResult validate(ValidationContext ctx, JsonNode node) {
        ValidationResult result = validator.validate(ctx, node);
        if (result.isValid()) {
            System.out.println(node.getJsonPointer() + ", " + this.node.getJsonPointer() + " - VALID");
        } else {
            System.out.println(node.getJsonPointer() + ", " + this.node.getJsonPointer() + " - " + result.getErrorMessage());
        }
        return result;
    }
}
