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
        OFFICIAL_DIALECTS = Collections.unmodifiableMap(map);
    }

    /**
     * Dialect corresponding to <i>draft2020-12</i> specification.
     */
    public static class Draft2020Dialect implements Dialect {
        private final EvaluatorFactory evaluatorFactory;
        private final Set<String> requiredVocabularies;
        private final Map<String, Boolean> defaultVocabularyObject;

        public Draft2020Dialect() {
            this.evaluatorFactory = new Draft2020EvaluatorFactory();
            this.requiredVocabularies = singleton(Draft2020.CORE);
            Map<String, Boolean> vocabs = new HashMap<>();
            vocabs.put(Draft2020.CORE, true);
            vocabs.put(Draft2020.APPLICATOR, true);
            vocabs.put(Draft2020.UNEVALUATED, true);
            vocabs.put(Draft2020.VALIDATION, true);
            vocabs.put(Draft2020.META_DATA, true);
            vocabs.put(Draft2020.FORMAT_ANNOTATION, true);
            vocabs.put(Draft2020.CONTENT, true);
            this.defaultVocabularyObject = unmodifiableMap(vocabs);
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

    /**
     * Dialect corresponding to <i>draft2019-09</i> specification.
     */
    public static class Draft2019Dialect implements Dialect {
        private final EvaluatorFactory evaluatorFactory;
        private final Set<String> requiredVocabularies;
        private final Map<String, Boolean> defaultVocabularyObject;

        public Draft2019Dialect() {
            this.evaluatorFactory = new Draft2019EvaluatorFactory();
            this.requiredVocabularies = singleton(Draft2019.CORE);
            Map<String, Boolean> vocabs = new HashMap<>();
            vocabs.put(Draft2019.CORE, true);
            vocabs.put(Draft2019.APPLICATOR, true);
            vocabs.put(Draft2019.VALIDATION, true);
            vocabs.put(Draft2019.META_DATA, true);
            vocabs.put(Draft2019.FORMAT, false);
            vocabs.put(Draft2019.CONTENT, true);
            this.defaultVocabularyObject = unmodifiableMap(vocabs);
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return SpecificationVersion.DRAFT2019_09;
        }

        @Override
        public String getMetaSchema() {
            return SpecificationVersion.DRAFT2019_09.getId();
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

    /**
     * Dialect corresponding to <i>draft7</i> specification.
     */
    public static class Draft7Dialect implements Dialect {
        private final EvaluatorFactory evaluatorFactory;

        public Draft7Dialect() {
            this.evaluatorFactory = new Draft7EvaluatorFactory();
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return SpecificationVersion.DRAFT7;
        }

        @Override
        public String getMetaSchema() {
            return SpecificationVersion.DRAFT7.getId();
        }

        @Override
        public EvaluatorFactory getEvaluatorFactory() {
            return evaluatorFactory;
        }

        @Override
        public Set<String> getSupportedVocabularies() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getRequiredVocabularies() {
            return Collections.emptySet();
        }

        @Override
        public Map<String, Boolean> getDefaultVocabularyObject() {
            return Collections.emptyMap();
        }
    }
}
