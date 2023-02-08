package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EnumValidator extends BasicValidator {
    private final List<JsonNode> enumNodes;

    EnumValidator(JsonNode constNode) {
        super("Enum validation failed.");
        List<JsonNode> temp = new ArrayList<>();
        for (JsonNode element : constNode.asArray()) {
            temp.add(element);
        }
        this.enumNodes = Collections.unmodifiableList(temp);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return enumNodes.stream()
                .anyMatch(node::isEqualTo);
    }
}
