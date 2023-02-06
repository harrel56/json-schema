package org.harrel.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        String schema = """
                {
                    "maxLength": 30,
                    "minLength": 1,
                    "pattern": "^a.*b$"
                }
                """;
        String schema2 = """
                {
                    "const": "1"
                }
                """;

        ValidatorFactory validatorFactory = new ValidatorFactory();
        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode schemaNode = objectMapper.readTree(schema);
        JsonNode schemaNode2 = new JacksonNode(objectMapper.readTree(schema2));

//        List<Validator> validators = new ArrayList<>();
//        for (Map.Entry<String, JsonNode> entry : schemaNode2.asObject()) {
//            validatorFactory.fromField(entry.getKey(), entry.getValue()).ifPresent(validators::add);
//        }
//
//
//        JsonNode jsonNode2 = new JacksonNode(objectMapper.readTree("\"1\""));
//
//        System.out.println(validators.stream().allMatch(c -> c.validate(jsonNode2)));

//        Check checker = createCheck(schemaNode.get("type").asText());
//        List<Check> stringChecks = getStringChecks(schemaNode);
//
//        String json = """
//                "a535345b"
//                """;
//        JsonNode jsonNode = objectMapper.readTree(json);
//        System.out.println(stringChecks.stream().allMatch(check -> check.check(jsonNode)));

    }
//
//    private static Check createCheck(String type) {
//        if ("string".equals(type)) {
//            return JsonNode::isTextual;
//        } else {
//            throw new UnsupportedOperationException();
//        }
//    }
//
//    private static List<Check> getStringChecks(JsonNode node) {
//        List<Check> checks = new ArrayList<>();
//        if (node.has("maxLength")) {
//            int maxLength = node.get("maxLength").asInt();
//            checks.add(new StringCheck() {
//                @Override
//                protected boolean stringCheck(String string) {
//                    return string.length() <= maxLength;
//                }
//            });
//        }
//        if (node.has("minLength")) {
//            int minLength = node.get("minLength").asInt();
//            checks.add(new StringCheck() {
//                @Override
//                protected boolean stringCheck(String string) {
//                    return string.length() >= minLength;
//                }
//            });
//        }
//        if (node.has("pattern")) {
//            Pattern pattern = Pattern.compile(node.get("pattern").asText());
//            checks.add(new StringCheck() {
//                @Override
//                protected boolean stringCheck(String string) {
//                    return pattern.matcher(string).matches();
//                }
//            });
//        }
//        return checks;
//    }
}