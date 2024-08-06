package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldResolveAllSpecificationMetaSchemasWithoutFragment(SpecificationVersion spec) {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(UriUtil.removeEmptyFragment(spec.getId()).toString());

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.toJsonNode(new JacksonNode.Factory())).isPresent();
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldHandleNonExistentSubSchemas(SpecificationVersion spec) {
        URI uri = URI.create(spec.getId()).resolve("meta/not-found");
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(uri.toString());

        assertThat(result.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void shouldHandleBaseSpecUris(SpecificationVersion spec) {
        URI uri = URI.create(spec.getId()).resolve(".");
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(uri.toString());

        assertThat(result.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getDraft2019SubSchemas")
    void shouldResolveDraft2019SubSchemas(URI uri) {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(uri.toString());

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

    static Stream<Arguments> getDraft2019SubSchemas() {
        return Stream.of(
                        "meta/applicator",
                        "meta/content",
                        "meta/core",
                        "meta/format",
                        "meta/meta-data",
                        "meta/validation"
                )
                .map(uri -> URI.create(SpecificationVersion.DRAFT2019_09.getId()).resolve(uri))
                .map(Arguments::of);
    }
}