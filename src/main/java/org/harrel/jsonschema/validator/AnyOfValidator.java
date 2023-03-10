package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.StreamUtil;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;

class AnyOfValidator extends BasicValidator {
    private final List<String> jsonPointers;

    AnyOfValidator(SchemaParsingContext ctx, JsonNode node) {
        super("None of the schemas matched.");
        this.jsonPointers = node.asArray().stream().map(ctx::getAbsoluteUri).toList();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        return StreamUtil.exhaustiveAnyMatch(jsonPointers.stream(), pointer -> ctx.resolveRequiredSchema(pointer).validate(ctx, node));
    }
}
