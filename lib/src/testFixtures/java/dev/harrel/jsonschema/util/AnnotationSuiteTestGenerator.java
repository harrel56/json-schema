package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.Annotation;
import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.SpecificationVersion;
import dev.harrel.jsonschema.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class AnnotationSuiteTestGenerator {
    private final SpecificationVersion specificationVersion;
    private final ProviderMapper mapper;
    private final Validator validator;
    private final Map<String, Set<String>> skippedTests;

    public AnnotationSuiteTestGenerator(SpecificationVersion specificationVersion,
                                        ProviderMapper mapper,
                                        Validator validator,
                                        Map<String, Set<String>> skippedTests) {
        this.specificationVersion = specificationVersion;
        this.mapper = mapper;
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
            List<AnnotationTestBundle> bundles = mapper.readAnnotationTestBundles(Files.readString(path));
            return bundles.stream().map(bundle -> readBundle(path.getFileName().toString(), bundle));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DynamicNode readBundle(String fileName, AnnotationTestBundle bundle) {
        Set<SpecificationVersion> supportedVersions = getSupportedVersion(bundle.compatibility());
        if (!supportedVersions.contains(specificationVersion)) {
            return dynamicContainer(bundle.description(), Stream.of());
        }
        return dynamicContainer(
                bundle.description(),
                bundle.tests().stream().map(testCase -> readTestCase(fileName, bundle, testCase))
        );
    }

    private DynamicNode readTestCase(String fileName, AnnotationTestBundle bundle, AnnotationTestCase testCase) {
        boolean skipped = skippedTests.getOrDefault(fileName, Set.of())
                .contains(bundle.description());

        return dynamicTest(testCase.instance().toPrintableString(), () ->
                testValidation(fileName, bundle.description(), bundle.schema(), testCase.instance(), testCase.assertions(), skipped));
    }

    private Set<SpecificationVersion> getSupportedVersion(String compatibility) {
        if (compatibility == null) {
            return EnumSet.allOf(SpecificationVersion.class);
        }
        Set<SpecificationVersion> res = EnumSet.noneOf(SpecificationVersion.class);
        switch (compatibility) {
            case "3":
            case "4":
                res.add(SpecificationVersion.DRAFT4);
            case "6":
                res.add(SpecificationVersion.DRAFT6);
            case "7":
                res.add(SpecificationVersion.DRAFT7);
            case "2019":
                res.add(SpecificationVersion.DRAFT2019_09);
            case "2020":
                res.add(SpecificationVersion.DRAFT2020_12);
                break;
            default:
                throw new IllegalArgumentException("Unsupported compatibility: " + compatibility);
        }
        return res;
    }

    private void testValidation(String fileName, String bundle, JsonNode schema, JsonNode instance,
                                List<AnnotationAssertion> assertions, boolean skipped) {
        Assumptions.assumeFalse(skipped);
//        Assumptions.assumeTrue(bundle.equals("schema that uses custom metaschema with with no validation vocabulary"));
//        Assumptions.assumeTrue(name.equals("no validation: invalid number, but it still validates"));

//        logger.info("%s: %s".formatted(bundle, name));
//        logger.info(String.valueOf(valid));

        Validator.Result res = validator.validate(validator.registerSchema(schema), instance);
        assertThat(res.isValid()).isTrue();
        assertThat(assertions).allSatisfy(assertion -> {
            List<Annotation> annotations = res.getAnnotations().stream().filter(anno ->
                            anno.getInstanceLocation().equals(assertion.location()) && anno.getKeyword().equals(assertion.keyword()))
                    .toList();
            if (assertion.expected().isEmpty()) {
                assertThat(annotations).isEmpty();
            } else {
                assertThat(assertion.expected()).allSatisfy((location, value) -> {
                    Optional<Object> annotation = annotations.stream()
                            .filter(anno -> anno.getSchemaLocation().endsWith(location))
                            .map(Annotation::getAnnotation)
                            .findFirst();
                    assertThat(annotation).hasValue(value);
                });
            }
        });
    }
}
