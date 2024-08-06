package dev.harrel.jsonschema;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code SpecificationVersion} enum represents JSON Schema specification versions that are supported.
 */
public enum SpecificationVersion {
    DRAFT2020_12(9, "https://json-schema.org/draft/2020-12/schema", "/draft2020-12.json") {
        @Override
        Optional<String> resolveResource(String uri) {
            return getId().equals(uri) ? readFileResource(getResourcePath()) : Optional.empty();
        }
    },
    DRAFT2019_09(8, "https://json-schema.org/draft/2019-09/schema", "/dev/harrel/jsonschema/draft/2019-09/schema.json"),
    DRAFT7(7, "http://json-schema.org/draft-07/schema#", "/dev/harrel/jsonschema/draft-07/schema.json");
    private final int order;
    private final URI baseUri;
    private final URI id;
    private final String resourcePath;
    private final URI resourcePathUri;

    SpecificationVersion(int order, String id, String resourcePath) {
        this.order = order;
        this.id = URI.create(id);
        this.baseUri = UriUtil.getUriWithoutFragment(id).resolve(".");
        this.resourcePath = resourcePath;
        this.resourcePathUri = URI.create(resourcePath);
    }

    Optional<String> resolveResource(String uri) {
        if (!uri.startsWith(getBaseUri().toString())) {
            return Optional.empty();
        }
        String relativeUri = uri.substring(getBaseUri().toString().length());
        URI resolvedUri = getResourcePathUri().resolve(UriUtil.getUriWithoutFragment(relativeUri));
        return readFileResource(resolvedUri + ".json");
    }

    /**
     * Returns ID which could be resolved to meta-schema.
     *
     * @return specification version ID
     */
    public String getId() {
        return id.toString();
    }

    /**
     * Returns path to a classpath resource containing meta-schema.
     *
     * @return resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    int getOrder() {
        return order;
    }

    URI getBaseUri() {
        return baseUri;
    }

    URI getResourcePathUri() {
        return resourcePathUri;
    }

    private static Optional<String> readFileResource(String uri) {
        try (InputStream is = SpecificationVersion.class.getResourceAsStream(uri)) {
            if (is == null) {
                return Optional.empty();
            }
            return Optional.of(new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
