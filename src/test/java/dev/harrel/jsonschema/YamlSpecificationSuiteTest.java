package dev.harrel.jsonschema;

import dev.harrel.jsonschema.util.YamlRemoteSchemaResolver;

public abstract class YamlSpecificationSuiteTest extends SpecificationSuiteTest {

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
