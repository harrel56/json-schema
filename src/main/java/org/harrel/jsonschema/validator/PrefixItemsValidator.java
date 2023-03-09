package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.StreamUtil;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;
import java.util.stream.IntStream;

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

        List<JsonNode> elements = node.asArray();
        return StreamUtil.exhaustiveAllMatch(
                IntStream.range(0, elements.size()).limit(prefixRefs.size()).boxed(),
                idx -> ctx.resolveRequiredSchema(prefixRefs.get(idx)).validate(ctx, elements.get(idx))
        );
    }
}
