package dev.harrel.jsonschema.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SuiteTestGenerator {
    private static final Logger logger = Logger.getLogger("SuiteTestGenerator");

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Map<String, Map<String, Set<String>>> skippedTests;

    public SuiteTestGenerator(ObjectMapper objectMapper, Validator validator, Map<String, Map<String, Set<String>>> skippedTests) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.skippedTests = skippedTests;
    }

    public Stream<DynamicNode> generate(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Resource not found");
            }
            Path path = Paths.get(url.toURI());
            if (Files.isRegularFile(path)) {
                return Stream.of(dynamicContainer(path.getFileName().toString(), readTestFile(path)));
            }
            return Files.list(path)
                    .filter(Files::isRegularFile)
                    .map(p -> dynamicContainer(p.getFileName().toString(), readTestFile(p)));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Stream<DynamicNode> readTestFile(Path path) {
        try {
            List<TestBundle> bundles = objectMapper.readValue(Files.readString(path), new TypeReference<>() {});
            return bundles.stream().map(bundle -> readBundle(path.getFileName().toString(), bundle));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DynamicNode readBundle(String fileName, TestBundle bundle) {
        return dynamicContainer(
                bundle.description,
                bundle.tests.stream().map(testCase -> readTestCase(fileName, bundle, testCase))
        );
    }

    private DynamicNode readTestCase(String fileName, TestBundle bundle, TestCase testCase) {
        boolean skipped = skippedTests.getOrDefault(stripExtension(fileName), Map.of())
                .getOrDefault(bundle.description, Set.of())
                .contains(testCase.description);

        return dynamicTest(testCase.description, () ->
                testValidation(bundle.description, testCase.description, bundle.schema, testCase.data, testCase.valid, skipped));
    }

    private String stripExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private record TestBundle(String description, JsonNode schema, List<TestCase> tests) {}

    private record TestCase(String description, JsonNode data, boolean valid) {}

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode instance, boolean valid, boolean skipped) throws JsonProcessingException {
        Assumptions.assumeFalse(skipped);
//        Assumptions.assumeTrue(bundle.equals("unevaluatedProperties with $dynamicRef"));
//        Assumptions.assumeTrue(name.equals("with no unevaluated properties"));

        String schemaString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        String instanceString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
        logger.info("%s: %s".formatted(bundle, name));
        logger.info(schemaString);
        logger.info(instanceString);
        logger.info(String.valueOf(valid));

        URI uri = validator.registerSchema(schemaString);
        Assertions.assertEquals(valid, validator.validate(uri, instanceString).isValid());
    }
}
