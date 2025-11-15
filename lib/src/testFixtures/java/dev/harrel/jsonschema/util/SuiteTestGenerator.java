package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SuiteTestGenerator {
    private static final Logger logger = Logger.getLogger("SuiteTestGenerator");

    private final ProviderMapper mapper;
    private final Validator validator;
    private final Map<String, Map<String, Set<String>>> skippedTests;

    public SuiteTestGenerator(ProviderMapper mapper, Validator validator, Map<String, Map<String, Set<String>>> skippedTests) {
        this.mapper = mapper;
        this.validator = validator;
        this.skippedTests = skippedTests;
    }

    public Stream<DynamicNode> generate(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream("/files.index");
            String[] resources = new String(is.readAllBytes()).split(System.lineSeparator());
            List<String> matchingResources = Arrays.stream(resources)
                    .filter(file -> file.startsWith(resourcePath))
                    .toList();
            System.out.println(resourcePath);
            System.out.println(matchingResources.size());
            if (matchingResources.isEmpty()) {
                throw new IllegalArgumentException("Resource not found");
            }
            return matchingResources.stream()
                    .map(Path::of)
                    .map(path -> dynamicContainer(path.getFileName().toString(), readTestResource(path)));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Stream<DynamicNode> readTestResource(Path resourcePath) {
        try {
            byte[] content = getClass().getResourceAsStream(resourcePath.toString()).readAllBytes();
            List<TestBundle> bundles = mapper.readTestBundles(new String(content));
            return bundles.stream().map(bundle -> readBundle(resourcePath.getFileName().toString(), bundle));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DynamicNode readBundle(String fileName, TestBundle bundle) {
        return dynamicContainer(
                bundle.description(),
                bundle.tests().stream().map(testCase -> readTestCase(fileName, bundle, testCase))
        );
    }

    private DynamicNode readTestCase(String fileName, TestBundle bundle, TestCase testCase) {
        boolean skipped = skippedTests.getOrDefault(stripExtension(fileName), Map.of())
                .getOrDefault(bundle.description(), Set.of())
                .contains(testCase.description());

        return dynamicTest(testCase.description(), () ->
                testValidation(bundle.description(), testCase.description(), bundle.schema(), testCase.data(), testCase.valid(), skipped));
    }

    private String stripExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private void testValidation(String bundle, String name, JsonNode schema, JsonNode instance, boolean valid, boolean skipped) {
        Assumptions.assumeFalse(skipped);
//        Assumptions.assumeTrue(bundle.equals("schema that uses custom metaschema with with no validation vocabulary"));
//        Assumptions.assumeTrue(name.equals("no validation: invalid number, but it still validates"));

//        logger.info("%s: %s".formatted(bundle, name));
//        logger.info(String.valueOf(valid));

        URI uri = validator.registerSchema(schema);
        Assertions.assertEquals(valid, validator.validate(uri, instance).isValid());
    }
}
