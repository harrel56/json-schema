package dev.harrel.jsonschema;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class VocabularyValidator {
    void validateVocabularies(Dialect dialect, Map<String, Boolean> vocabularyObject) {
        if (vocabularyObject == null) {
            return;
        }

        List<String> missingRequiredVocabularies = dialect.getRequiredVocabularies().stream()
                .filter(vocab -> !vocabularyObject.getOrDefault(vocab, false))
                .collect(Collectors.toList());
        if (!missingRequiredVocabularies.isEmpty()) {
            throw new VocabularyException(String.format("Required vocabularies %s were missing or marked optional in $vocabulary object", missingRequiredVocabularies));
        }
        List<String> unsupportedRequiredVocabularies = vocabularyObject.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .filter(vocab -> !dialect.getSupportedVocabularies().contains(vocab))
                .collect(Collectors.toList());
        if (!unsupportedRequiredVocabularies.isEmpty()) {
            throw new VocabularyException(String.format("Following vocabularies %s are required but not supported", unsupportedRequiredVocabularies));
        }
    }
}
