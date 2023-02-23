package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.Collections;
import java.util.List;

class EnumValidator extends BasicValidator {
    private final List<JsonNode> enumNodes;

    EnumValidator(JsonNode constNode) {
        super("Enum validation failed.");
        this.enumNodes = Collections.unmodifiableList(constNode.asArray());
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return enumNodes.stream().anyMatch(node::isEqualTo);
    }
}
