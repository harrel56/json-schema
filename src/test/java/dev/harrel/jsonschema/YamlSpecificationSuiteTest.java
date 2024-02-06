package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.harrel.jsonschema.util.RemoteSchemaResolver;
import dev.harrel.jsonschema.util.SuiteTestGenerator;
import dev.harrel.jsonschema.util.YamlRemoteSchemaResolver;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class YamlSpecificationSuiteTest extends SpecificationSuiteTest {

    @Override
    ObjectMapper createObjectMapper() {
        return new YAMLMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    SchemaResolver createSchemaResolver() {
        return new YamlRemoteSchemaResolver();
    }

    @Override
    String getTestPath() {
        return "/suite-yaml/tests";
    }

    @Override
    String getFileExtension() {
        return ".yml";
    }
}
