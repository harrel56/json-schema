package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.util.SuiteTestGenerator;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

public abstract class SupplementarySuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> draft2020Supplementary() {
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(createObjectMapper(), validator, Map.of());
        return generator.generate("/suite-supplementary/draft2020-12");
    }

    @TestFactory
    Stream<DynamicNode> draft2019Supplementary() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(createObjectMapper(), validator, Map.of());
        return generator.generate("/suite-supplementary/draft2019-09");
    }

    @TestFactory
    Stream<DynamicNode> draft2020FormatSupplementary() {
        Validator validator = new ValidatorFactory()
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(createObjectMapper(), validator, Map.of());
        return generator.generate("/suite-supplementary/draft2020-12/format");
    }

    @TestFactory
    Stream<DynamicNode> draft2019FormatSupplementary() {
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2019Dialect())
                .withEvaluatorFactory(new FormatEvaluatorFactory())
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        SuiteTestGenerator generator = new SuiteTestGenerator(createObjectMapper(), validator, Map.of());
        return generator.generate("/suite-supplementary/draft2019-09/format");
    }

    private ObjectMapper createObjectMapper() {
        return  new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
