package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;

import java.net.URI;
import java.util.List;

public class IdentifiableSchema extends Schema {
    private final URI uri;

    public IdentifiableSchema(URI uri, List<Validator> validators) {
        super(validators);
        this.uri = uri;
    }

    @Override
    public boolean validate(ValidationContext ctx, JsonNode node) {
        return super.validate(ctx.withParentSchema(this), node);
    }

    public URI getUri() {
        return uri;
    }
}
