package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.AnnotationSuiteTestGenerator;
import dev.harrel.jsonschema.util.ProviderMapper;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

public abstract class AnnotationSuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> draft2020AnnotationTests() {
        Dialect dialect = new Dialects.Draft2020Dialect();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDefaultDialect(dialect)
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(dialect.getSpecificationVersion(),
                new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }

    @TestFactory
    Stream<DynamicNode> draft2019AnnotationTests() {
        Dialect dialect = new Dialects.Draft2019Dialect();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDefaultDialect(dialect)
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(dialect.getSpecificationVersion(),
                new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }

    @TestFactory
    Stream<DynamicNode> draft7AnnotationTests() {
        Dialect dialect = new Dialects.Draft7Dialect();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDefaultDialect(dialect)
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(dialect.getSpecificationVersion(),
                new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }

    @TestFactory
    Stream<DynamicNode> draft6AnnotationTests() {
        Dialect dialect = new Dialects.Draft6Dialect();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDefaultDialect(dialect)
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(dialect.getSpecificationVersion(),
                new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }

    @TestFactory
    Stream<DynamicNode> draft4AnnotationTests() {
        Dialect dialect = new Dialects.Draft4Dialect();
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .withDefaultDialect(dialect)
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(dialect.getSpecificationVersion(),
                new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }
}
