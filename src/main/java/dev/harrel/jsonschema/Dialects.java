package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static dev.harrel.jsonschema.Vocabulary.*;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

/**
 * Static container class for all officially supported dialects.
 */
public final class Dialects {
    private Dialects() {}

    static final Map<URI, Dialect> OFFICIAL_DIALECTS;

    static {
        Map<URI, Dialect> map = new HashMap<>();
        map.put(URI.create(SpecificationVersion.DRAFT2020_12.getId()), new Draft2020Dialect());
        map.put(URI.create(SpecificationVersion.DRAFT2019_09.getId()), new Draft2019Dialect());
        map.put(UriUtil.removeEmptyFragment(SpecificationVersion.DRAFT7.getId()), new Draft7Dialect());
        map.put(UriUtil.removeEmptyFragment(SpecificationVersion.DRAFT6.getId()), new Draft6Dialect());
        OFFICIAL_DIALECTS = Collections.unmodifiableMap(map);
    }

    /**
     * Dialect corresponding to <i>draft2020-12</i> specification.
     */
    public static class Draft2020Dialect extends BaseDialect {
        public Draft2020Dialect() {
            super(new Draft2020EvaluatorFactory(), singleton(Draft2020.CORE), createDefaultVocabularyObject());
        }

        private static Map<String, Boolean> createDefaultVocabularyObject() {
            Map<String, Boolean> vocabs = new HashMap<>();
            vocabs.put(Draft2020.CORE, true);
            vocabs.put(Draft2020.APPLICATOR, true);
            vocabs.put(Draft2020.UNEVALUATED, true);
            vocabs.put(Draft2020.VALIDATION, true);
            vocabs.put(Draft2020.META_DATA, true);
            vocabs.put(Draft2020.FORMAT_ANNOTATION, true);
            vocabs.put(Draft2020.CONTENT, true);
            return unmodifiableMap(vocabs);
        }
    }

    /**
     * Dialect corresponding to <i>draft2019-09</i> specification.
     */
    public static class Draft2019Dialect extends BaseDialect {
        public Draft2019Dialect() {
            super(new Draft2019EvaluatorFactory(), singleton(Draft2019.CORE), createDefaultVocabularyObject());
        }

        private static Map<String, Boolean> createDefaultVocabularyObject() {
            Map<String, Boolean> vocabs = new HashMap<>();
            vocabs.put(Draft2019.CORE, true);
            vocabs.put(Draft2019.APPLICATOR, true);
            vocabs.put(Draft2019.VALIDATION, true);
            vocabs.put(Draft2019.META_DATA, true);
            vocabs.put(Draft2019.FORMAT, false);
            vocabs.put(Draft2019.CONTENT, true);
            return unmodifiableMap(vocabs);
        }
    }

    /**
     * Dialect corresponding to <i>draft7</i> specification.
     */
    public static class Draft7Dialect extends BaseDialect {
        public Draft7Dialect() {
            super(new Draft7EvaluatorFactory(), Collections.emptySet(), Collections.emptyMap());
        }
    }

    /**
     * Dialect corresponding to <i>draft6</i> specification.
     */
    public static class Draft6Dialect extends BaseDialect {
        public Draft6Dialect() {
            super(new Draft6EvaluatorFactory(), Collections.emptySet(), Collections.emptyMap());
        }
    }

    private static abstract class BaseDialect implements Dialect {
        private final EvaluatorFactory evaluatorFactory;
        private final Set<String> requiredVocabularies;
        private final Map<String, Boolean> defaultVocabularyObject;

        public BaseDialect(EvaluatorFactory evaluatorFactory, Set<String> requiredVocabularies, Map<String, Boolean> defaultVocabularyObject) {
            this.evaluatorFactory = evaluatorFactory;
            this.requiredVocabularies = requiredVocabularies;
            this.defaultVocabularyObject = defaultVocabularyObject;
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return SpecificationVersion.DRAFT2020_12;
        }

        @Override
        public String getMetaSchema() {
            return SpecificationVersion.DRAFT2020_12.getId();
        }

        @Override
        public EvaluatorFactory getEvaluatorFactory() {
            return evaluatorFactory;
        }

        @Override
        public Set<String> getSupportedVocabularies() {
            return defaultVocabularyObject.keySet();
        }

        @Override
        public Set<String> getRequiredVocabularies() {
            return requiredVocabularies;
        }

        @Override
        public Map<String, Boolean> getDefaultVocabularyObject() {
            return defaultVocabularyObject;
        }
    }
}
