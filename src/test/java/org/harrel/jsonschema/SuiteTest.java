package org.harrel.jsonschema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

record SchemaTest(String description, JsonNode schema, List<TestData> tests) {}

record TestData(String description, JsonNode data, boolean valid) {}

class SuiteTest {

    @ParameterizedTest(name = "[{index}] {0}: {1}")
    @ArgumentsSource(SuiteProvider.class)
    @SuiteFile("/draft2020-12/type.json")
    void name(String bundle, String name, JsonNode schema, JsonNode json, boolean valid) {
        SchemaValidator validator = new SchemaValidator();
        Assertions.assertEquals(valid, validator.validate(new JacksonNode(schema), new JacksonNode(json)));
    }
}

class SuiteProvider implements ArgumentsProvider {

    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) throws Exception {
        SuiteFile suiteFile = context.getRequiredTestMethod().getAnnotation(SuiteFile.class);
        try (InputStream is = getClass().getResourceAsStream(suiteFile.value())) {
            List<SchemaTest> bundles = new ObjectMapper().readValue(is, new TypeReference<>() {});
            return bundles.stream()
                    .flatMap(bundle -> bundle.tests().stream().map(test ->
                            Arguments.arguments(bundle.description(), test.description(), bundle.schema(), test.data(), test.valid())));
        }
    }
}
