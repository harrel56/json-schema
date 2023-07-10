package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.harrel.jsonschema.ValidatorFactory.DefaultSchemaResolver;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultSchemaResolverTest {

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldResolveAllSpecificationMetaSchemas(SpecificationVersion spec) {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(spec.getId());

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.toJsonNode(new JacksonNode.Factory())).isPresent();
    }

    @Test
    void shouldHandleNonExistentResource() {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve("unknown");

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldCacheResults() {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        resolver.resolve(SpecificationVersion.DRAFT2020_12.getId());
        resolver.resolve(SpecificationVersion.DRAFT2020_12.getId());
        SchemaResolver.Result result = resolver.resolve(SpecificationVersion.DRAFT2020_12.getId());

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.toJsonNode(new JacksonNode.Factory())).isPresent();
    }
}