package dev.harrel.jsonschema.util;

import java.net.URI;

public class YamlRemoteSchemaResolver extends RemoteSchemaResolver {
    @Override
    String uriToResource(URI uri) {
        String path = uri.getPath();
        if (path.endsWith(".json")) {
            return "/suite-yaml/remotes" + path.substring(0, path.length() - 5) + ".yml";
        }
        return "/suite-yaml/remotes" + path;
    }
}
