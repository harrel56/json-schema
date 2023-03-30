package org.harrel.jsonschema.providers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

class JacksonTest {
    @Nested
    class SpecificationTest extends org.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends org.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class CoreValidatorFactoryTest extends org.harrel.jsonschema.CoreValidatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }

    @Nested
    class MetaSchemaTest extends org.harrel.jsonschema.MetaSchemaTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new JacksonNode.Factory();
        }
    }
}
