package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.Map;
import java.util.Set;

class UnevaluatedPropertiesValidator extends BasicValidator {
    private final String schemaPointer;

    UnevaluatedPropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("UnevaluatedProperties validation failed.");
        this.schemaPointer = ctx.getAbsoluteUri(node);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        String jsonPointer = node.getJsonPointer();
        Set<String> evaluatedPaths = ctx.getEvaluatedPaths();
        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            if (evaluatedPaths.stream().noneMatch(path -> path.startsWith(jsonPointer + entry.getKey()))) {
                valid = valid && ctx.resolveRequiredSchema(schemaPointer).validate(ctx, entry.getValue());
            }
        }
        return valid;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
