package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.math.BigInteger;
import java.util.Optional;

class ContainsValidator extends BasicValidator {
    private final String schemaUri;
    private final boolean minContainsZero;

    ContainsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Contains validation failed.");
        this.schemaUri = ctx.getAbsoluteUri(node);
        this.minContainsZero = Optional.ofNullable(ctx.getCurrentSchemaObject().get("minContains"))
                .map(JsonNode::asInteger)
                .map(BigInteger::intValueExact)
                .map(min -> min == 0)
                .orElse(false);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }

        Schema schema = ctx.resolveRequiredSchema(schemaUri);
        long count = node.asArray().stream()
                .filter(element -> schema.validate(ctx, element))
                .count();
        return count > 0 || minContainsZero;
    }
}
