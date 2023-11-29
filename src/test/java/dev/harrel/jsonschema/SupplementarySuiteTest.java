package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.SuiteTestGenerator;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

public abstract class SupplementarySuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> draft2020Supplementary() {
        Validator validator = new ValidatorFactory()
                .withDisabledSchemaValidation(true)
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, Map.of());
        return generator.generate("/suite-supplementary");
    }

    @TestFactory
    Stream<DynamicNode> draft2020FormatSupplementary() {
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, Map.of());
        return generator.generate("/suite-supplementary/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2019FormatSupplementary() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(validator, Map.of());
        return generator.generate("/suite-supplementary/format");
    }
}
