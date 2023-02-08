package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.*;

class RequiredValidator extends BasicValidator {
    private final List<String> requiredProperties;

    RequiredValidator(JsonNode constNode) {
        super("Required properties validation failed.");
        List<String> temp = new ArrayList<>();
        for (JsonNode element : constNode.asArray()) {
            temp.add(element.asString());
        }
        this.requiredProperties = Collections.unmodifiableList(temp);
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
