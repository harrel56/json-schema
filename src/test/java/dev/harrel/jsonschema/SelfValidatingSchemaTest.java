package dev.harrel.jsonschema;

import org.junit.jupiter.api.Test;

import java.net.URI;

class SelfValidatingSchemaTest {

    static class FileResolver implements SchemaResolver {
        @Override
        public Result resolve(String uri) {
            return switch (uri) {
                case "https://json-schema.org/draft/2020-12/schema" -> Result.fromString(TestUtil.readResource("/draft/2020-12/schema.json"));
                case "https://json-schema.org/draft/2020-12/meta/core.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/core.json"));
                case "https://json-schema.org/draft/2020-12/meta/applicator.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/applicator.json"));
                case "https://json-schema.org/draft/2020-12/meta/unevaluated.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/unevaluated.json"));
                case "https://json-schema.org/draft/2020-12/meta/validation.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/validation.json"));
                case "https://json-schema.org/draft/2020-12/meta/meta-data.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/meta-data.json"));
                case "https://json-schema.org/draft/2020-12/meta/format-annotation.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/format-annotation.json"));
                case "https://json-schema.org/draft/2020-12/meta/content.json" -> Result.fromString(TestUtil.readResource("/draft/2020-12/meta/content.json"));
                default -> Result.empty();
            };
        }
    }

    @Test
    void name() {
        FileResolver fileResolver = new FileResolver();
        Validator validator = new ValidatorFactory().withSchemaResolver(fileResolver).createValidator();
        // currently results in stack overflow :( - schema validation should support such cases
        validator.validate(URI.create("https://json-schema.org/draft/2020-12/schema"), "{}");
    }
}