package org.harrel.jsonschema;

import java.net.URI;
import java.util.List;

public class IdentifiableSchema extends Schema {
    private final URI uri;

    public IdentifiableSchema(URI uri, List<ValidatorDelegate> validators) {
        super(validators);
        this.uri = uri;
    }

    @Override
    public boolean validate(ValidationContext ctx, JsonNode node) {
        ctx.dynamicScope.push(this);
        boolean result = super.validate(ctx, node);
        ctx.dynamicScope.pop();
        return result;
    }

    public URI getUri() {
        return uri;
    }
}
