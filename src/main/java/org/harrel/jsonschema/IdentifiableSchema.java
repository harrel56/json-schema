package org.harrel.jsonschema;

import java.net.URI;
import java.util.List;

public class IdentifiableSchema extends Schema {
    private final URI uri;

    public IdentifiableSchema(URI uri, List<ValidatorDelegate> validators, String dynamicAnchor) {
        super(validators, dynamicAnchor);
        this.uri = uri;
    }

    @Override
    public boolean validate(ValidationContext ctx, JsonNode node) {
        ValidationContext newCtx = ctx.withParentSchema(this);
        newCtx.ctxes.push(ctx);
        boolean result = super.validate(newCtx, node);
        newCtx.ctxes.pop();
        return result;
    }

    public URI getUri() {
        return uri;
    }
}
