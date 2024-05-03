package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.ProviderMapper;
import dev.harrel.jsonschema.util.SuiteTestGenerator;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

public abstract class SupplementarySuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> draft2020Supplementary() {
        ValidatorFactory factory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory());

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), factory, Map.of());
        return generator.generate("/suite-supplementary/draft2020-12");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Supplementary() {
        ValidatorFactory factory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory());

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), factory, Map.of());
        return generator.generate("/suite-supplementary/draft2019-09");
    }

    @TestFactory
    Stream<DynamicNode> draft2020FormatSupplementary() {
        ValidatorFactory factory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory());

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), factory, Map.of());
        return generator.generate("/suite-supplementary/draft2020-12/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2019FormatSupplementary() {
        ValidatorFactory factory = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory());

        SuiteTestGenerator generator = new SuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), factory, Map.of());
        return generator.generate("/suite-supplementary/draft2019-09/format");
    }
}
