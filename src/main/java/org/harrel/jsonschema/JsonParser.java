package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;
import org.harrel.jsonschema.validator.ValidatorFactory;

import java.net.URI;
import java.util.*;

public class JsonParser {

    private static final Set<String> NOT_PARSABLE_KEYWORDS = Set.of("const", "enum");

    private final ValidatorFactory validatorFactory;
    private final AnnotationCollector<?> collector;

    public JsonParser(ValidatorFactory validatorFactory, AnnotationCollector<?> collector) {
        this.validatorFactory = validatorFactory;
        this.collector = collector;
    }

    public SchemaParsingContext parseRootSchema(String baseUri, JsonNode node) {
        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(baseUri);
            URI uri = URI.create(baseUri);
            ctx.registerIdentifiableSchema(uri, node, List.of(Schema.getBooleanValidator(node.asBoolean())));
            return ctx;
        } else {
            Map<String, JsonNode> objectMap = node.asObject();
            String id = Optional.ofNullable(objectMap.get("$id"))
                    .map(JsonNode::asString)
                    .orElse(baseUri);
            SchemaParsingContext ctx = new SchemaParsingContext(id);
            URI uri = URI.create(id);
            List<Validator> validators = parseValidators(ctx, objectMap);
            ctx.registerIdentifiableSchema(uri, node, validators);
            return ctx;
        }
    }

    private void parseNode(SchemaParsingContext ctx, JsonNode node) {
        if (node.isBoolean()) {
            parseBoolean(ctx, node);
        } else if (node.isArray()) {
            parseArray(ctx, node);
        } else if (node.isObject()) {
            parseObject(ctx, node);
        }
    }

    private void parseBoolean(SchemaParsingContext ctx, JsonNode node) {
        Validator booleanValidator = Schema.getBooleanValidator(node.asBoolean());
        ctx.registerSchema(node, List.of(new ReportingValidator(collector, node, booleanValidator)));
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        if (objectMap.containsKey("$id")) {
            URI uri = ctx.getParentUri().resolve(objectMap.get("$id").asString());
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<Validator> validators = parseValidators(newCtx, objectMap);
            newCtx.registerIdentifiableSchema(uri, node, validators);
        } else {
            ctx.registerSchema(node, parseValidators(ctx, objectMap));
        }
    }

    private List<Validator> parseValidators(SchemaParsingContext ctx, Map<String, JsonNode> object) {
        SchemaParsingContext newCtx = ctx.withCurrentSchemaContext(object);
        List<Validator> validators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            validatorFactory.fromField(newCtx, entry.getKey(), entry.getValue())
                    .map(validator -> new ReportingValidator(collector, entry.getValue(), validator))
                    .ifPresent(validators::add);
            if (!NOT_PARSABLE_KEYWORDS.contains(entry.getKey())) {
                parseNode(newCtx, entry.getValue());
            }
        }
        return validators;
    }
}

