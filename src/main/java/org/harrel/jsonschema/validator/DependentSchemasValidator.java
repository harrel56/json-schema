package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.StreamUtil;
import org.harrel.jsonschema.ValidationContext;

import java.util.Map;
import java.util.stream.Collectors;

class DependentSchemasValidator extends BasicValidator {
    private final Map<String, String> dependentSchemas;

    DependentSchemasValidator(SchemaParsingContext ctx, JsonNode node) {
        super("DependentSchemas validation failed.");
        this.dependentSchemas = node.asObject().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ctx.getAbsoluteUri(e.getValue())));
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        return StreamUtil.exhaustiveAllMatch(
                node.asObject().keySet()
                        .stream()
                        .filter(dependentSchemas::containsKey)
                        .map(dependentSchemas::get)
                        .map(ctx::resolveRequiredSchema),
                schema -> schema.validate(ctx, node)
        );
    }
}
