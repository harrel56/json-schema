package org.harrel.jsonschema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

class SuiteArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) throws Exception {
        SuiteTest suiteTest = context.getRequiredTestMethod().getAnnotation(SuiteTest.class);
        try (InputStream is = getClass().getResourceAsStream(suiteTest.value())) {
            List<SchemaTest> bundles = getObjectMapper().readValue(is, new TypeReference<>() {});
            return bundles.stream()
                    .flatMap(bundle -> bundle.tests().stream().map(test ->
                            Arguments.arguments(bundle.description(), test.description(), bundle.schema(), test.data(), test.valid())));
        }
    }

    private ObjectMapper getObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    record SchemaTest(String description, JsonNode schema, List<TestData> tests) {}

    record TestData(String description, JsonNode data, boolean valid) {}
}
