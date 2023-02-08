package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.*;

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
        Set<String> properties = new HashSet<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject()) {
            properties.add(entry.getKey());
        }
        return properties.containsAll(requiredProperties);
    }
}
