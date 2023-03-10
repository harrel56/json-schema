package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.ArrayList;
import java.util.List;

class UniqueItemsValidator extends BasicValidator {
    private final boolean unique;

    UniqueItemsValidator(JsonNode node) {
        super("UniqueItems validation failed.");
        this.unique = node.asBoolean();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray() || !unique) {
            return true;
        }
        List<JsonNode> parsed = new ArrayList<>();
        for (JsonNode element : node.asArray()) {
            if (parsed.stream().anyMatch(element::isEqualTo)) {
                return false;
            }
            parsed.add(element);
        }
        return true;
    }
}
