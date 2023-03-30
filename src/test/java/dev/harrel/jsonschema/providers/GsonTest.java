package dev.harrel.jsonschema.providers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

class GsonTest {
    @Nested
    class SpecificationTest extends dev.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends dev.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class CoreValidatorFactoryTest extends dev.harrel.jsonschema.CoreValidatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }
    @Nested
    class MetaSchemaTest extends dev.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

}
