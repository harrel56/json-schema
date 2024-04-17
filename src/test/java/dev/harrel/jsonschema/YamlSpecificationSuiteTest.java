package dev.harrel.jsonschema;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.harrel.jsonschema.util.YamlRemoteSchemaResolver;

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
