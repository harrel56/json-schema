package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeSchemaResolverTest {

    @Test
    void shouldReturnEmptyForNoResolvers() {
        SchemaResolver composedResolver = SchemaResolver.compose();
        SchemaResolver.Result result = composedResolver.resolve(SpecificationVersion.DRAFT2020_12.getId());
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldComposeMultipleResolvers() {
        SchemaResolver.Result result1 = SchemaResolver.Result.fromString("{}");
        SchemaResolver resolver1 = uri -> {
            if ("x".equals(uri)) {
                return result1;
            } else {
                return SchemaResolver.Result.empty();
            }
        };
        SchemaResolver.Result result2 = SchemaResolver.Result.fromString("null");
        SchemaResolver resolver2 = uri -> {
            if ("y".equals(uri)) {
                return result2;
            } else {
                return SchemaResolver.Result.empty();
            }
        };
        SchemaResolver.Result result3 = SchemaResolver.Result.fromString("true");
        SchemaResolver resolver3 = uri -> {
            if ("z".equals(uri)) {
                return result3;
            } else {
                return SchemaResolver.Result.empty();
            }
        };

        SchemaResolver composedResolver = SchemaResolver.compose(resolver1, resolver2, resolver3);
        assertThat(composedResolver.resolve("x")).isEqualTo(result1);
        assertThat(composedResolver.resolve("y")).isEqualTo(result2);
        assertThat(composedResolver.resolve("z")).isEqualTo(result3);
    }

    @Test
    void shouldReturnFirstNonEmptyResult() {
        SchemaResolver.Result result1 = SchemaResolver.Result.fromString("{}");
        SchemaResolver resolver1 = uri -> {
            if ("x".equals(uri)) {
                return result1;
            } else {
                return SchemaResolver.Result.empty();
            }
        };
        SchemaResolver resolver2 = uri -> {
            if ("x".equals(uri)) {
                return SchemaResolver.Result.fromString("null");
            } else {
                return SchemaResolver.Result.empty();
            }
        };
        SchemaResolver resolver3 = uri -> {
            if ("x".equals(uri)) {
                return SchemaResolver.Result.fromString("true");
            } else {
                return SchemaResolver.Result.empty();
            }
        };

        SchemaResolver composedResolver = SchemaResolver.compose(resolver1, resolver2, resolver3);
        assertThat(composedResolver.resolve("x")).isEqualTo(result1);
    }
}