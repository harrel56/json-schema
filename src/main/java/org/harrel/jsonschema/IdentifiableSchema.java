package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;

import java.util.List;

public class IdentifiableSchema extends Schema {
    private final String id;

    public IdentifiableSchema(String id, List<Validator> validators) {
        super(validators);
        this.id = id;
    }

    @Override
    public boolean validate(ValidationContext ctx, JsonNode node) {
        return super.validate(ctx.withParentSchema(this), node);
    }

    public String getId() {
        return id;
    }
}
