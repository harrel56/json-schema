package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.Schema;
import org.harrel.jsonschema.SchemaParsingContext;
import org.harrel.jsonschema.ValidationContext;

class ItemsValidator extends BasicValidator {
    private final String itemSchemaRef;
    private final int prefixItemsSize;

    ItemsValidator(SchemaParsingContext ctx, JsonNode node) {
        super("Items validation failed.");
        this.itemSchemaRef = ctx.getAbsoluteUri(node);
        JsonNode prefixItems = ctx.getCurrentSchemaObject().get("prefixItems");
        this.prefixItemsSize = prefixItems == null ? 0 : prefixItems.asArray().size();
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isArray()) {
            return true;
        }
        Schema schema = ctx.resolveRequiredSchema(itemSchemaRef);
        return node.asArray().stream()
                .skip(prefixItemsSize)
                .allMatch(element -> schema.validate(ctx, element));
    }
}
