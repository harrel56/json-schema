package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class RequiredValidator extends BasicValidator {
    private final List<String> requiredProperties;

    RequiredValidator(JsonNode node) {
        super("Required properties validation failed.");
        this.requiredProperties = node.asArray().stream().map(JsonNode::asString).toList();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return node.asObject().keySet().containsAll(requiredProperties);
    }
}
