package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class DialectsTest {
    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void officiallySupportedMapContainsAllSpecVersions(SpecificationVersion version) {
        Dialect dialect = Dialects.OFFICIAL_DIALECTS.get(UriUtil.removeEmptyFragment(version.getId()));
        assertThat(dialect).isNotNull();
        assertThat(dialect.getSpecificationVersion()).isEqualTo(version);
    }
}
