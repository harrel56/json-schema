package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class OneOfValidator extends BasicValidator {
    private final List<String> jsonPointers;

    OneOfValidator(SchemaParsingContext ctx, JsonNode node) {
        super("OneOf validation failed");
        this.jsonPointers = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return jsonPointers.stream()
                .filter(uri -> ctx.resolveRequiredSchema(uri).validate(ctx, node))
                .count() == 1;
    }
}
