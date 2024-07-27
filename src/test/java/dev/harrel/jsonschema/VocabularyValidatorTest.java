package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VocabularyValidatorTest {
    private final VocabularyValidator validator = new VocabularyValidator();

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldAlwaysPassForNullVocabs(SpecificationVersion version) {
        validator.validateVocabularies(createDialect(version, orderedSet(), orderedSet()), null);
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldAlwaysPassForEmptyRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet("urn:a", "urn:b"), orderedSet());
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", true), Map.entry("urn:b", false));

        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#strictVersions")
    void shouldFailForMissingRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b"));
        Map<String, Boolean> vocabs = orderedMap();

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b] were missing or marked optional in $vocabulary object");
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#lenientVersions")
    void shouldPassForMissingRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b"));
        validator.validateVocabularies(dialect, orderedMap());
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#strictVersions")
    void shouldFailForOptionalRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b"));
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b] were missing or marked optional in $vocabulary object");
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#lenientVersions")
    void shouldPassForOptionalRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b"));
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));
        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#strictVersions")
    void shouldFailForOptionalAndMissingRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b", "urn:c"));
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b, urn:c] were missing or marked optional in $vocabulary object");
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#lenientVersions")
    void shouldPassForOptionalAndMissingRequiredVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet("urn:a", "urn:b", "urn:c"));
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));
        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#strictVersions")
    void shouldFailForMandatoryUnsupportedVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet());
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", true), Map.entry("urn:b", true));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Following vocabularies [urn:a, urn:b] are required but not supported");
    }

    @ParameterizedTest
    @MethodSource("dev.harrel.jsonschema.IdKeywordTest#lenientVersions")
    void shouldPassForMandatoryUnsupportedVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet());
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", true), Map.entry("urn:b", true));
        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldPassUnusedMandatoryVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(Vocabulary.Draft2020.CORE, "urn:a", "urn:b"), orderedSet(Vocabulary.Draft2020.CORE));
        Map<String, Boolean> vocabs = orderedMap(Map.entry(Vocabulary.Draft2020.CORE, true));
        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldPassForOptionalUnsupportedVocabs(SpecificationVersion version) {
        Dialect dialect = createDialect(version, orderedSet(), orderedSet());
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));
        validator.validateVocabularies(dialect, vocabs);
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void allOfficialDialectsShouldBeInternallyValid(SpecificationVersion version) {
        Dialect dialect = Dialects.OFFICIAL_DIALECTS.get(URI.create(version.getId()));
        validator.validateVocabularies(dialect, dialect.getDefaultVocabularyObject());
    }

    private static Dialect createDialect(SpecificationVersion version, Set<String> supportedVocabs, Set<String> requiredVocabs) {
        return new Dialects.Draft2020Dialect() {
            @Override
            public SpecificationVersion getSpecificationVersion() {
                return version;
            }

            @Override
            public Set<String> getSupportedVocabularies() {
                return supportedVocabs;
            }

            @Override
            public Set<String> getRequiredVocabularies() {
                return requiredVocabs;
            }
        };
    }

    private static Set<String> orderedSet(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    @SafeVarargs
    private static Map<String, Boolean> orderedMap(Map.Entry<String, Boolean>... values) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : values) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}