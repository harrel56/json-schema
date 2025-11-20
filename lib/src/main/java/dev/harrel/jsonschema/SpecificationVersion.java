package dev.harrel.jsonschema;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * {@code SpecificationVersion} enum represents JSON Schema specification versions that are supported.
 */
public enum SpecificationVersion {
    DRAFT2020_12(9, "https://json-schema.org/draft/2020-12/schema", "/draft2020-12.json", emptyList()),
    DRAFT2019_09(8, "https://json-schema.org/draft/2019-09/schema", "/dev/harrel/jsonschema/draft/2019-09/schema.json",
            Arrays.asList("meta/applicator", "meta/content", "meta/core", "meta/format", "meta/meta-data", "meta/validation")),
    DRAFT7(7, "http://json-schema.org/draft-07/schema#", "/dev/harrel/jsonschema/draft-07/schema.json", emptyList()),
    DRAFT6(6, "http://json-schema.org/draft-06/schema#", "/dev/harrel/jsonschema/draft-06/schema.json", emptyList()),
    DRAFT4(4, "http://json-schema.org/draft-04/schema#", "/dev/harrel/jsonschema/draft-04/schema.json", emptyList());
    private final int order;
    private final URI id;
    private final URI resourcePath;
    private final Map<URI, URI> additionalResources;

    SpecificationVersion(int order, String id, String resourcePath, List<String> additionalPaths) {
        this.order = order;
        this.id = URI.create(id);
        this.resourcePath = URI.create(resourcePath);

        Map<URI, URI> resourceMap = new HashMap<>();
        resourceMap.put(UriUtil.getUriWithoutFragment(this.id), getLastPathSegmentWithoutExtension(this.resourcePath));
        for (String additionalPath : additionalPaths) {
            URI additionalUri = URI.create(additionalPath);
            resourceMap.put(this.id.resolve(additionalPath), additionalUri);
        }
        this.additionalResources = unmodifiableMap(resourceMap);
    }

    Optional<String> resolveResource(URI uri) {
        URI normalizedUri = UriUtil.getUriWithoutFragment(uri);
        URI relativePath = additionalResources.get(normalizedUri);
        if (relativePath == null) {
            return Optional.empty();
        }
        return readFileResource(resourcePath.resolve(relativePath) + ".json");
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
        return resourcePath.toString();
    }

    int getOrder() {
        return order;
    }

    private static URI getLastPathSegmentWithoutExtension(URI uri) {
        int start = uri.getPath().lastIndexOf('/');
        int end = uri.getPath().lastIndexOf('.');
        return URI.create(uri.getPath().substring(start + 1, end));
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
