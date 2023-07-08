package dev.harrel.jsonschema;

import java.util.Map;
import java.util.Set;

interface Dialect {
    SpecificationVersion getSpecificationVersion();

    String getMetaSchema();

    EvaluatorFactory getEvaluatorFactory();


    Set<String> getSupportedVocabularies();

    Set<String> getRequiredVocabularies();

    Map<String, Boolean> getDefaultVocabularyObject();
}
