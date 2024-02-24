package dev.harrel.jsonschema;

import java.io.*;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code SpecificationVersion} enum represents JSON Schema specification versions that are supported.
 */
public enum SpecificationVersion {
    DRAFT2020_12("https://json-schema.org/draft/2020-12/schema"),
    DRAFT2019_09("https://json-schema.org/draft/2019-09/schema");
    private final URI id;

    SpecificationVersion(String id) {
        this.id = URI.create(id);
    }

    Optional<String> resolveResource(URI uri) {
        if (!uri.toString().startsWith(getId().resolve("").toString())) {
            return Optional.empty();
        }
        return readFileResource(getResourceLocation(uri));
    }

    /**
     * Returns ID which could be resolved to meta-schema.
     *
     * @return specification version ID
     */
    public URI getId() {
        return id;
    }

    /**
     * Returns path to a classpath resource containing meta-schema.
     *
     * @return resource path
     */
    public URI getResourcePath() {
        return URI.create(getResourceLocation(id));
    }

    private static String getResourceLocation(URI uri) {
        return "/dev/harrel/jsonschema" +  uri.getPath() + ".json";
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
