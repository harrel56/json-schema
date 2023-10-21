package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static dev.harrel.jsonschema.util.TestUtil.assertError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class SelfValidatingSchemaTest {

    static class FileResolver implements SchemaResolver {
        @Override
        public Result resolve(String uri) {
            return switch (uri) {
                case "https://json-schema.org/draft/2020-12/schema" -> Result.fromString(TestUtil.readResource("/draft/2020-12/schema.json"));
                case "https://json-schema.org/draft/2020-12/meta/core" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/core.json"));
                case "https://json-schema.org/draft/2020-12/meta/applicator" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/applicator.json"));
                case "https://json-schema.org/draft/2020-12/meta/unevaluated" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/unevaluated.json"));
                case "https://json-schema.org/draft/2020-12/meta/validation" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/validation.json"));
                case "https://json-schema.org/draft/2020-12/meta/meta-data" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/meta-data.json"));
                case "https://json-schema.org/draft/2020-12/meta/format-annotation" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/format-annotation.json"));
                case "https://json-schema.org/draft/2020-12/meta/content" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/content.json"));
                default -> Result.empty();
            };
        }
    }

    @Test
    void isValidAgainstItself() {
        FileResolver fileResolver = new FileResolver();
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(fileResolver)
                .createValidator();

        Validator.Result result = validator.validate(
                URI.create("https://json-schema.org/draft/2020-12/schema"),
                TestUtil.readResource("/draft/2020-12/schema.json")
        );

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validatesInvalidInstance() {
        FileResolver fileResolver = new FileResolver();
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(fileResolver)
                .createValidator();

        String instance = """
                {
                  "type": {}
                }
                """;
        Validator.Result result = validator.validate(URI.create("https://json-schema.org/draft/2020-12/schema"), instance);

        assertThat(result.isValid()).isFalse();
        List<Error> errors = result.getErrors();
        assertThat(errors).hasSize(4);
        assertError(
                errors.get(0),
                "/allOf/3/$ref/properties/type/anyOf/0/$ref/enum",
                "https://json-schema.org/draft/2020-12/meta/validation#/$defs/simpleTypes",
                "/type",
                "enum",
                "Expected any of [[array, boolean, integer, null, number, object, string]]"
        );
    }

    @Test
    void failsForInvalidSchemaPart() {
        SchemaResolver invalidResolver = uri -> {
            if (uri.equals("https://json-schema.org/draft/2020-12/meta/content")) {
                return SchemaResolver.Result.fromString(TestUtil.readResource("/draft/2020-12/meta/invalid-content.json"));
            }
            return SchemaResolver.Result.empty();
        };
        SchemaResolver resolver = SchemaResolver.compose(invalidResolver, new FileResolver());
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(resolver)
                .createValidator();

        URI schemaUri = URI.create("https://json-schema.org/draft/2020-12/schema");
        String instance = TestUtil.readResource("/draft/2020-12/schema.json");
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.validate(schemaUri, instance), InvalidSchemaException.class);

        List<Error> errors = exception.getErrors();
        assertThat(errors).hasSize(2);
        assertError(
                errors.get(0),
                "/allOf/0/$ref/properties/$vocabulary/type",
                "https://json-schema.org/draft/2020-12/meta/core#/properties/$vocabulary",
                "/$vocabulary",
                "type",
                "Value is [string] but should be [object]"
        );
        assertError(
                errors.get(1),
                "/allOf",
                "https://json-schema.org/draft/2020-12/schema#",
                "",
                "allOf",
                "Value does not match against the schemas at indexes [0]"
        );
    }

    @Test
    void failsForInvalidRootSchema() {
        SchemaResolver invalidResolver = uri -> {
            if (uri.equals("https://json-schema.org/draft/2020-12/schema")) {
                return SchemaResolver.Result.fromString(TestUtil.readResource("/draft/2020-12/invalid-schema.json"));
            }
            return SchemaResolver.Result.empty();
        };
        SchemaResolver resolver = SchemaResolver.compose(invalidResolver, new FileResolver());
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(resolver)
                .createValidator();

        URI schemaUri = URI.create("https://json-schema.org/draft/2020-12/schema");
        String instance = TestUtil.readResource("/draft/2020-12/schema.json");
        InvalidSchemaException exception = catchThrowableOfType(() -> validator.validate(schemaUri, instance), InvalidSchemaException.class);

        List<Error> errors = exception.getErrors();
        assertThat(errors).hasSize(5);
        assertError(
                errors.get(0),
                "/allOf/1/$ref/properties/properties/additionalProperties/$dynamicRef/allOf/3/$ref/properties/type/anyOf/0/$ref/enum",
                "https://json-schema.org/draft/2020-12/meta/validation#/$defs/simpleTypes",
                "/properties/definitions/type",
                "enum",
                "Expected any of [[array, boolean, integer, null, number, object, string]]"
        );
        assertError(
                errors.get(1),
                "/allOf/1/$ref/properties/properties/additionalProperties/$dynamicRef/allOf/3/$ref/properties/type/anyOf/1/type",
                "https://json-schema.org/draft/2020-12/meta/validation#/properties/type/anyOf/1",
                "/properties/definitions/type",
                "type",
                "Value is [integer] but should be [array]"
        );
        assertError(
                errors.get(2),
                "/allOf/1/$ref/properties/properties/additionalProperties/$dynamicRef/allOf/3/$ref/properties/type/anyOf",
                "https://json-schema.org/draft/2020-12/meta/validation#/properties/type",
                "/properties/definitions/type",
                "anyOf",
                "Value does not match against any of the schemas"
        );
        assertError(
                errors.get(3),
                "/allOf/1/$ref/properties/properties/additionalProperties/$dynamicRef/allOf",
                "https://json-schema.org/draft/2020-12/schema#",
                "/properties/definitions",
                "allOf",
                "Value does not match against the schemas at indexes [3]"
        );
        assertError(
                errors.get(4),
                "/allOf",
                "https://json-schema.org/draft/2020-12/schema#",
                "",
                "allOf",
                "Value does not match against the schemas at indexes [1]"
        );
    }
}