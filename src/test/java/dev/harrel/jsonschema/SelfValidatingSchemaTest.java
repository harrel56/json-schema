package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static dev.harrel.jsonschema.TestUtil.assertError;
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
    void failsForInvalidSchema() {
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
    }
}