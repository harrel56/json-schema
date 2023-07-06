package dev.harrel.jsonschema;

import java.util.*;

import static dev.harrel.jsonschema.Vocabulary.Draft2020.*;

/**
 * {@code Vocabulary} class exposes constants related to vocabularies.
 */
public final class Vocabulary {
    private Vocabulary() {}

    static final Set<String> APPLICATOR_VOCABULARY = Collections.singleton(APPLICATOR);
    static final Set<String> UNEVALUATED_VOCABULARY = Collections.singleton(UNEVALUATED);
    static final Set<String> VALIDATION_VOCABULARY = Collections.singleton(VALIDATION);
    static final Map<String, Boolean> DEFAULT_VOCABULARIES_OBJECT;
    static {
        Map<String, Boolean> vocabs = new HashMap<>();
        vocabs.put(CORE, true);
        vocabs.put(APPLICATOR, true);
        vocabs.put(UNEVALUATED, true);
        vocabs.put(VALIDATION, true);
        vocabs.put(META_DATA, true);
        vocabs.put(FORMAT_ANNOTATION, true);
        vocabs.put(CONTENT, true);
        DEFAULT_VOCABULARIES_OBJECT = Collections.unmodifiableMap(vocabs);
    }

    /**
     * {@code Vocabulary.Draft2020} class exposes vocabulary URIs that are part of a draft2020-12 specification.
     */
    public static final class Draft2020 {
        private Draft2020() {}

        public static final String CORE = "https://json-schema.org/draft/2020-12/vocab/core";
        public static final String APPLICATOR = "https://json-schema.org/draft/2020-12/vocab/applicator";
        public static final String UNEVALUATED = "https://json-schema.org/draft/2020-12/vocab/unevaluated";
        public static final String VALIDATION = "https://json-schema.org/draft/2020-12/vocab/validation";
        public static final String META_DATA = "https://json-schema.org/draft/2020-12/vocab/meta-data";
        public static final String FORMAT_ANNOTATION = "https://json-schema.org/draft/2020-12/vocab/format-annotation";
        public static final String CONTENT = "https://json-schema.org/draft/2020-12/vocab/content";
    }
}
