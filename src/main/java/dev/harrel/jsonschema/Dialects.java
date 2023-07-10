package dev.harrel.jsonschema;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static dev.harrel.jsonschema.Vocabulary.Draft2020.*;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

/**
 * Static container class for all officially supported dialects.
 */
public class Dialects {
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
            this.requiredVocabularies = singleton(CORE);
            Map<String, Boolean> vocabs = new HashMap<>();
            vocabs.put(CORE, true);
            vocabs.put(APPLICATOR, true);
            vocabs.put(UNEVALUATED, true);
            vocabs.put(VALIDATION, true);
            vocabs.put(META_DATA, true);
            vocabs.put(FORMAT_ANNOTATION, true);
            vocabs.put(CONTENT, true);
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
}
