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
    ValidationContext adjustValidationContext(ValidationContext ctx) {
        return ctx.withParentSchema(this);
    }
}
