package org.harrel.jsonschema.providers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

class GsonTest {
    @Nested
    class SpecificationTest extends org.harrel.jsonschema.SpecificationTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class JsonNodeTest extends org.harrel.jsonschema.JsonNodeTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }

    @Nested
    class CoreValidatorFactoryTest extends org.harrel.jsonschema.CoreValidatorFactoryTest {
        @BeforeAll
        static void beforeAll() {
            nodeFactory = new GsonNode.Factory();
        }
    }
}
