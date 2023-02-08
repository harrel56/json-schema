package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

class ConstValidator extends BasicValidator {
    private final JsonNode constNode;

    ConstValidator(JsonNode constNode) {
        super("Const validation failed.");
        this.constNode = constNode;
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return constNode.isEqualTo(node);
    }
}
