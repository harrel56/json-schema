package dev.harrel.jsonschema;

import java.util.*;

import static java.util.Collections.*;

/**
 * {@code Vocabulary} class exposes constants related to vocabularies.
 */
public final class Vocabulary {
    private Vocabulary() {}

    /**
     * {@code Vocabulary.Draft2020} class exposes vocabulary URIs that are part of a <i>draft2020-12</i> specification.
     */
    public static final class Draft2020 {
        private Draft2020() {}

        public static final String CORE = "https://json-schema.org/draft/2020-12/vocab/core";
        public static final String APPLICATOR = "https://json-schema.org/draft/2020-12/vocab/applicator";
        public static final String UNEVALUATED = "https://json-schema.org/draft/2020-12/vocab/unevaluated";
        public static final String VALIDATION = "https://json-schema.org/draft/2020-12/vocab/validation";
        public static final String META_DATA = "https://json-schema.org/draft/2020-12/vocab/meta-data";
        public static final String FORMAT_ANNOTATION = "https://json-schema.org/draft/2020-12/vocab/format-annotation";
        public static final String FORMAT_ASSERTION = "https://json-schema.org/draft/2020-12/vocab/format-assertion";
        public static final String CONTENT = "https://json-schema.org/draft/2020-12/vocab/content";
    }

    /**
     * {@code Vocabulary.Draft2019} class exposes vocabulary URIs that are part of a <i>draft2019-09</i> specification.
     */
    public static final class Draft2019 {
        private Draft2019() {}

        public static final String CORE = "https://json-schema.org/draft/2019-09/vocab/core";
        public static final String APPLICATOR = "https://json-schema.org/draft/2019-09/vocab/applicator";
        public static final String VALIDATION = "https://json-schema.org/draft/2019-09/vocab/validation";
        public static final String META_DATA = "https://json-schema.org/draft/2019-09/vocab/meta-data";
        public static final String FORMAT = "https://json-schema.org/draft/2019-09/vocab/format";
        public static final String CONTENT = "https://json-schema.org/draft/2019-09/vocab/content";
    }
}
