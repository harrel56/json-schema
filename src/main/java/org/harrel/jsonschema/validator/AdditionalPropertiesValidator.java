package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

class AdditionalPropertiesValidator extends BasicValidator {
    private final String additionalPropertiesRef;
    private final Set<String> declaredProperties;

    AdditionalPropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("AdditionalProperties validation failed.");
        this.additionalPropertiesRef = ctx.getAbsoluteUri(node);
        this.declaredProperties = Optional.ofNullable(ctx.getCurrentSchemaObject().get("properties"))
                .map(JsonNode::asObject)
                .map(Map::keySet)
                .orElse(Set.of());
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }
        Schema schema = ctx.resolveRequiredSchema(additionalPropertiesRef);
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            if (!declaredProperties.contains(entry.getKey())) {
                valid = valid && schema.validate(ctx, entry.getValue());
            }
        }
        return valid;
    }
}
