package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VocabularyValidatorTest {
    private final VocabularyValidator validator = new VocabularyValidator();

    @Test
    void shouldAlwaysPassForNullVocabs() {
        validator.validateVocabularies(new Dialects.Draft2020Dialect(), null);
    }

    @Test
    void shouldAlwaysPassForEmptyRequiredVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet();
            }

            @Override
            public Set<String> getSupportedVocabularies() {
                return orderedSet("urn:a", "urn:b");
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", true), Map.entry("urn:b", false));

        validator.validateVocabularies(dialect, vocabs);
    }

    @Test
    void shouldFailForMissingRequiredVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet("urn:a", "urn:b");
            }
        };
        Map<String, Boolean> vocabs = orderedMap();

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b] were missing or marked optional in $vocabulary object");
    }

    @Test
    void shouldFailForOptionalRequiredVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet("urn:a", "urn:b");
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b] were missing or marked optional in $vocabulary object");
    }

    @Test
    void shouldFailForOptionalAndMissingRequiredVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet("urn:a", "urn:b", "urn:c");
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Required vocabularies [urn:a, urn:b, urn:c] were missing or marked optional in $vocabulary object");
    }

    @Test
    void shouldPassUnusedMandatoryVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getSupportedVocabularies() {
                return orderedSet(Vocabulary.Draft2020.CORE, "urn:a", "urn:b");
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry(Vocabulary.Draft2020.CORE, true));

        validator.validateVocabularies(dialect, vocabs);
    }

    @Test
    void shouldPassForOptionalUnsupportedVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet();
            }

            @Override
            public Set<String> getSupportedVocabularies() {
                return orderedSet();
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", false), Map.entry("urn:b", false));

        validator.validateVocabularies(dialect, vocabs);
    }

    @Test
    void shouldFailForMandatoryUnsupportedVocabs() {
        Dialects.Draft2020Dialect dialect = new Dialects.Draft2020Dialect() {
            @Override
            public Set<String> getRequiredVocabularies() {
                return orderedSet();
            }

            @Override
            public Set<String> getSupportedVocabularies() {
                return orderedSet();
            }
        };
        Map<String, Boolean> vocabs = orderedMap(Map.entry("urn:a", true), Map.entry("urn:b", true));

        assertThatThrownBy(() -> validator.validateVocabularies(dialect, vocabs))
                .isInstanceOf(VocabularyException.class)
                .hasMessage("Following vocabularies [urn:a, urn:b] are required but not supported");
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void allOfficialDialectsShouldBeInternallyValid(SpecificationVersion version) {
        Dialect dialect = Dialects.OFFICIAL_DIALECTS.get(URI.create(version.getId()));
        validator.validateVocabularies(dialect, dialect.getDefaultVocabularyObject());
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