package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class DependentRequiredValidator extends BasicValidator {
    private final Map<String, List<String>> requiredProperties;

    DependentRequiredValidator(JsonNode node) {
        super("DependentRequired validation failed.");
        this.requiredProperties = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toStringList(e.getValue())));
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        Set<String> objectKeys = node.asObject().keySet();
        return objectKeys
                .stream()
                .filter(requiredProperties::containsKey)
                .map(requiredProperties::get)
                .flatMap(List::stream)
                .allMatch(objectKeys::contains);
    }

    private List<String> toStringList(JsonNode node) {
        return node.asArray().stream().map(JsonNode::asString).toList();
    }
}
