package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
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
    void shouldResolveAllSpecificationMetaSchemasWitFragment(SpecificationVersion spec) {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        String uri = UriUtil.removeEmptyFragment(spec.getId()) + "#";
        SchemaResolver.Result result = resolver.resolve(uri);

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
    @EnumSource(SpecificationVersion.class)
    void allMetaSchemaResourcesShouldExist(SpecificationVersion spec) {
        InputStream is = this.getClass().getResourceAsStream(spec.getResourcePath());
        assertThat(is).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("getDraft2019SubSchemas")
    void shouldResolveDraft2019SubSchemas(URI uri) {
        DefaultSchemaResolver resolver = new DefaultSchemaResolver();
        SchemaResolver.Result result = resolver.resolve(uri.toString());

        assertThat(result.isEmpty()).isFalse();
        Optional<JsonNode> jsonNodeOptional = result.toJsonNode(new JacksonNode.Factory());
        assertThat(jsonNodeOptional).isPresent();
        JsonNode idNode = jsonNodeOptional.get().asObject().get(Keyword.ID);
        assertThat(idNode.isString()).isTrue();
        assertThat(idNode.asString()).isEqualTo(uri.toString());
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

    static Stream<String> getDraft2019SubSchemas() {
        return Stream.of(
                        "https://json-schema.org/draft/2019-09/meta/applicator",
                        "https://json-schema.org/draft/2019-09/meta/content",
                        "https://json-schema.org/draft/2019-09/meta/core",
                        "https://json-schema.org/draft/2019-09/meta/format",
                        "https://json-schema.org/draft/2019-09/meta/meta-data",
                        "https://json-schema.org/draft/2019-09/meta/validation"
                );
    }
}