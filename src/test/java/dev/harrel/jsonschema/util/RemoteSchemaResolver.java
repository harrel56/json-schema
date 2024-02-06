package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.SchemaResolver;

import java.net.URI;

import static dev.harrel.jsonschema.util.TestUtil.readResource;

public class RemoteSchemaResolver implements SchemaResolver {
    @Override
    public Result resolve(String uriString) {
        URI uri = URI.create(uriString);
        if (!uri.getHost().equals("localhost")) {
            return Result.empty();
        }

        String content = readResource(uriToResource(uri));
        return Result.fromString(content);
    }

    String uriToResource(URI uri) {
        return "/suite/remotes" + uri.getPath();
    }
}
