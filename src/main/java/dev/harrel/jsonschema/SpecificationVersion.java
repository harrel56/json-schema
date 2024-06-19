package dev.harrel.jsonschema;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static dev.harrel.jsonschema.Vocabulary.*;

/**
 * {@code SpecificationVersion} enum represents JSON Schema specification versions that are supported.
 */
public enum SpecificationVersion {
    DRAFT2020_12("https://json-schema.org/draft/2020-12", "/draft2020-12.json", new Draft2020EvaluatorFactory(),
           Arrays.asList(
                    Draft2020.CORE,
                    Draft2020.APPLICATOR,
                    Draft2020.UNEVALUATED,
                    Draft2020.VALIDATION,
                    Draft2020.META_DATA,
                    Draft2020.FORMAT_ANNOTATION,
                    Draft2020.CONTENT
            )) {
        @Override
        Optional<String> resolveResource(String uri) {
            return getId().equals(uri) ? readFileResource(getResourcePath()) : Optional.empty();
        }
    },
    DRAFT2019_09("https://json-schema.org/draft/2019-09", "/dev/harrel/jsonschema/draft/2019-09/schema.json", new Draft2019EvaluatorFactory(),
            Arrays.asList(
                    Draft2019.CORE,
                    Draft2019.APPLICATOR,
                    Draft2019.VALIDATION,
                    Draft2019.META_DATA,
                    Draft2019.FORMAT,
                    Draft2019.CONTENT
            ));
    private final URI baseUri;
    private final String id;
    private final String resourcePath;
    private final URI resourcePathUri;
    private final EvaluatorFactory evaluatorFactory;
    private final Set<String> activeVocabularies;

    private static final Map<URI, SpecificationVersion> BY_URI;

    static {
        Map<URI, SpecificationVersion> byUri = new HashMap<>();
        for (SpecificationVersion version : SpecificationVersion.values()) {
            byUri.put(URI.create(version.getId()), version);
        }
        BY_URI = Collections.unmodifiableMap(byUri);
    }

    // todo public + doc?
    static Optional<SpecificationVersion> fromUri(URI uri) {
        return Optional.ofNullable(BY_URI.get(Objects.requireNonNull(uri)));
    }

    SpecificationVersion(String baseUri, String resourcePath, EvaluatorFactory evaluatorFactory, Collection<String> activeVocabularies) {
        this.baseUri = URI.create(baseUri);
        this.id = baseUri + "/schema";
        this.resourcePath = resourcePath;
        this.resourcePathUri = URI.create(resourcePath);
        this.evaluatorFactory = evaluatorFactory;
        this.activeVocabularies = Collections.unmodifiableSet(new HashSet<>(activeVocabularies));
    }

    Optional<String> resolveResource(String uri) {
        if (!uri.startsWith(getBaseUri().toString())) {
            return Optional.empty();
        }
        String relativeUri = uri.substring(getBaseUri().toString().length() + 1);
        URI resolvedUri = getResourcePathUri().resolve(relativeUri);
        return readFileResource(resolvedUri + ".json");
    }

    /**
     * Returns ID which could be resolved to meta-schema.
     *
     * @return specification version ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns path to a classpath resource containing meta-schema.
     *
     * @return resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    // todo doc
    public EvaluatorFactory getEvaluatorFactory() {
        return evaluatorFactory;
    }

    // todo doc
    public Set<String> getActiveVocabularies() {
        return activeVocabularies;
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
