package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.harrel.jsonschema.util.SuiteTestGenerator.*;

public class ProviderMapper {
    private final JsonNodeFactory factory;

    public ProviderMapper(JsonNodeFactory factory) {
        this.factory = factory;
    }

    List<TestBundle> readTestBundles(String jsonString) {
        JsonNode rootNode = factory.create(jsonString);
        List<TestBundle> testBundles = new ArrayList<>();
        for (JsonNode arrayNode : rootNode.asArray()) {
            Map<String, JsonNode> bundle = arrayNode.asObject();
            String description = bundle.get("description").asString();
            JsonNode schema = factory.wrap(bundle.get("schema"));

            List<TestCase> tests = new ArrayList<>();
            for (JsonNode arrayNode2 : bundle.get("tests").asArray()) {
                Map<String, JsonNode> testCase = arrayNode2.asObject();
                String caseDescription = testCase.get("description").asString();
                JsonNode data = factory.wrap(testCase.get("data"));
                boolean valid = testCase.get("valid").asBoolean();
                tests.add(new TestCase(caseDescription, data, valid));
            }

            testBundles.add(new TestBundle(description, schema, tests));
        }
        return testBundles;
    }
}
