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

        String resourcePath = "/suite/remotes" + uri.getPath();
        String content = readResource(resourcePath);
        return Result.fromString(content);
    }
}
