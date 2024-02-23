package dev.harrel.jsonschema;

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
            return SpecificationVersion.DRAFT2020_12.getId().toString();
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
            return SpecificationVersion.DRAFT2019_09.getId().toString();
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
