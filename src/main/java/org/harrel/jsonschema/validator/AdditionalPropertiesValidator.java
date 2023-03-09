package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

class AdditionalPropertiesValidator extends BasicValidator {
    private final String additionalPropertiesRef;
    private final Set<String> declaredProperties;
    private final List<Pattern> declaredPatternProperties;

    AdditionalPropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("AdditionalProperties validation failed.");
        this.additionalPropertiesRef = ctx.getAbsoluteUri(node);
        this.declaredProperties = Optional.ofNullable(ctx.getCurrentSchemaObject().get("properties"))
                .map(JsonNode::asObject)
                .map(Map::keySet)
                .orElse(Set.of());
        this.declaredPatternProperties = Optional.ofNullable(ctx.getCurrentSchemaObject().get("patternProperties"))
                .map(JsonNode::asObject)
                .map(Map::keySet)
                .map(keySet -> keySet.stream().map(Pattern::compile).toList())
                .orElse(List.of());
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }
        Schema schema = ctx.resolveRequiredSchema(additionalPropertiesRef);
        return StreamUtil.exhaustiveAllMatch(
                node.asObject()
                        .entrySet()
                        .stream()
                        .filter(entry -> !declaredProperties.contains(entry.getKey()) && nonePatternsMatch(entry.getKey())),
                entry -> schema.validate(ctx, entry.getValue())
        );
    }

    private boolean nonePatternsMatch(String key) {
        return declaredPatternProperties.stream().noneMatch(p -> p.matcher(key).find());
    }
}
