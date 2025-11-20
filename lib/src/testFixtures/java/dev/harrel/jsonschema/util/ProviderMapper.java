package dev.harrel.jsonschema.util;

import dev.harrel.jsonschema.JsonNode;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.TestJsonNodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    List<AnnotationTestBundle> readAnnotationTestBundles(String jsonString) {
        JsonNode rootNode = factory.create(jsonString);
        List<AnnotationTestBundle> bundles = new ArrayList<>();
        for (JsonNode arrayNode : rootNode.asObject().get("suite").asArray()) {
            Map<String, JsonNode> bundle = arrayNode.asObject();
            String description = bundle.get("description").asString();
            String compatibility = Optional.ofNullable(bundle.get("compatibility"))
                    .map(JsonNode::asString)
                    .orElse(null);
            JsonNode schema = factory.wrap(bundle.get("schema"));

            List<AnnotationTestCase> tests = new ArrayList<>();
            for (JsonNode arrayNode2 : bundle.get("tests").asArray()) {
                Map<String, JsonNode> testCase = arrayNode2.asObject();
                JsonNode instance = factory.wrap(testCase.get("instance"));

                List<AnnotationAssertion> assertions = new ArrayList<>();
                for (JsonNode arrayNode3 : testCase.get("assertions").asArray()) {
                    Map<String, JsonNode> assertion = arrayNode3.asObject();
                    String location = assertion.get("location").asString();
                    String keyword = assertion.get("keyword").asString();
                    Map<String, Object> expected = assertion.get("expected").asObject().entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> TestJsonNodeUtil.getValue(e.getValue())));
                    assertions.add(new AnnotationAssertion(location, keyword, expected));
                }
                tests.add(new AnnotationTestCase(instance, assertions));
            }
            bundles.add(new AnnotationTestBundle(description, compatibility, schema, tests));
        }
        return bundles;
    }
}

record TestBundle(String description, JsonNode schema, List<TestCase> tests) {}

record TestCase(String description, JsonNode data, boolean valid) {}

record AnnotationTestBundle(String description, String compatibility, JsonNode schema, List<AnnotationTestCase> tests) {}

record AnnotationTestCase(JsonNode instance, List<AnnotationAssertion> assertions) {}

record AnnotationAssertion(String location, String keyword, Map<String, Object> expected) {}
