package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class PrefixItemsValidator extends BasicValidator {
    private final List<String> prefixRefs;

    PrefixItemsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("PrefixItems validation failed.");
        this.prefixRefs = node.asArray().stream()
                .map(ctx::getAbsoluteUri)
                .toList();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        boolean valid = true;
        List<JsonNode> elements = node.asArray();
        for (int i = 0; i < elements.size() && i < prefixRefs.size(); i++) {
            valid = valid && ctx.resolveRequiredSchema(prefixRefs.get(i)).validate(ctx, elements.get(i));
        }
        return valid;
    }
}
