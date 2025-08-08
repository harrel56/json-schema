package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.AnnotationSuiteTestGenerator;
import dev.harrel.jsonschema.util.ProviderMapper;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTestGenerator;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AnnotationSuiteTest implements ProviderTest {

    @TestFactory
    Stream<DynamicNode> annotationTests() {
        Validator validator = new ValidatorFactory()
                .withJsonNodeFactory(getJsonNodeFactory())
                .createValidator();

        AnnotationSuiteTestGenerator generator = new AnnotationSuiteTestGenerator(new ProviderMapper(getJsonNodeFactory()), validator, Map.of());
        return generator.generate("/suite-annotation");
    }
}
