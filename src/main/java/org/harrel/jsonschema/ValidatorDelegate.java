package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

class ValidatorDelegate implements Validator {
    private final JsonNode schemaNode;
    private final Validator validator;

    public ValidatorDelegate(JsonNode schemaNode, Validator validator) {
        this.schemaNode = schemaNode;
        this.validator = validator;
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode instanceNode) {
        return validator.validate(ctx, instanceNode);
    }

    @Override
    public int getOrder() {
        return validator.getOrder();
    }

    public String getKeywordPath() {
        return schemaNode.getJsonPointer();
    }
}
