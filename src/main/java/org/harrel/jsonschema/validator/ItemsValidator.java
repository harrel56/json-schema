package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

import java.util.List;
import java.util.Optional;

class ItemsValidator extends BasicValidator {
    private final String itemSchemaPointer;
    private final int prefixItemsSize;

    ItemsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Items validation failed.");
        this.itemSchemaPointer = ctx.getAbsoluteUri(node);
        this.prefixItemsSize = Optional.ofNullable(ctx.getCurrentSchemaObject().get("prefixItems"))
                .map(JsonNode::asArray)
                .map(List::size)
                .orElse(0);
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }
        Schema schema = ctx.resolveRequiredSchema(itemSchemaPointer);
        return node.asArray().stream()
                .skip(prefixItemsSize)
                .allMatch(element -> schema.validate(ctx, element));
    }
}
