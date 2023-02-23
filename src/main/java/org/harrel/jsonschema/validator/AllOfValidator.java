package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class AllOfValidator extends BasicValidator {
    private final List<String> jsonPointers;

    AllOfValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Some of the schemas didn't match.");
        this.jsonPointers = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return jsonPointers.stream()
                .allMatch(uri -> ctx.resolveRequiredSchema(uri).validate(ctx, node));
    }
}
