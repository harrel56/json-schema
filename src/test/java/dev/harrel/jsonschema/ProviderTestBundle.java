package dev.harrel.jsonschema;

import org.junit.jupiter.api.Nested;

public abstract class ProviderTestBundle implements ProviderTest {
    @Nested
    class SpecificationSuiteTest extends dev.harrel.jsonschema.SpecificationSuiteTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

        @Nested
    class SupplementarySuiteTest extends dev.harrel.jsonschema.SupplementarySuiteTest {
            @Override
            public JsonNodeFactory getJsonNodeFactory() {
                return ProviderTestBundle.this.getJsonNodeFactory();
            }
    }

    @Nested
    class Draft2020EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2020EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class Draft2019EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft2019EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class Draft7EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft7EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class Draft6EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft6EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class Draft4EvaluatorFactoryTest extends dev.harrel.jsonschema.Draft4EvaluatorFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class JsonNodeFactoryTest extends dev.harrel.jsonschema.JsonNodeFactoryTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class VocabulariesTest extends dev.harrel.jsonschema.VocabulariesTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class JsonPointerEscapingTest extends dev.harrel.jsonschema.JsonPointerEscapingTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class EvaluationPathTest extends dev.harrel.jsonschema.EvaluationPathTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }

    @Nested
    class DisabledSchemaValidationTest extends dev.harrel.jsonschema.DisabledSchemaValidationTest {
        @Override
        public JsonNodeFactory getJsonNodeFactory() {
            return ProviderTestBundle.this.getJsonNodeFactory();
        }
    }
}
