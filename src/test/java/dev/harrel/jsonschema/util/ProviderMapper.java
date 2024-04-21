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

    String toJsonString(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        toJsonString(node, sb);
        return sb.toString();
    }

    // todo this was just a poc, to be trashed
    private void toJsonString(JsonNode node, StringBuilder sb) {
        switch (node.getNodeType()) {
            case NULL -> sb.append("null");
            case BOOLEAN -> sb.append(node.asBoolean());
            case STRING -> sb.append("\"").append(node.asString()).append("\"");
            case INTEGER -> sb.append(node.asInteger());
            case NUMBER -> sb.append(node.asNumber());
            case ARRAY -> {
                sb.append("[").append(System.lineSeparator());
                for (JsonNode arrayNode : node.asArray()) {
                    toJsonString(arrayNode, sb);
                    sb.append(",").append(System.lineSeparator());
                }
                sb.delete(sb.length() - System.lineSeparator().length() - 1, sb.length());
                sb.append("]");
            }
            case OBJECT -> {
                sb.append("{").append(System.lineSeparator());
                for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
                    sb.append("\"").append(entry.getKey()).append("\": ");
                    toJsonString(entry.getValue(), sb);
                    sb.append(",").append(System.lineSeparator());
                }
                sb.delete(sb.length() - System.lineSeparator().length() - 1, sb.length());
                sb.append("}");
            }
        }
    }
}
