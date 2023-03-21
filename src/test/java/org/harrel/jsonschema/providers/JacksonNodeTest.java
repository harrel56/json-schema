package org.harrel.jsonschema.providers;

import org.junit.jupiter.api.BeforeAll;

public class JacksonNodeTest extends JsonNodeTest {
    @BeforeAll
    static void beforeAll() {
        nodeFactory = new JacksonNode.Factory();
    }
}
