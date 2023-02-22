package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.Validator;
import org.harrel.jsonschema.validator.ValidatorFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonParser {

    private final ValidatorFactory validatorFactory;
    private final ValidationCollector<?> collector;

    public JsonParser(ValidatorFactory validatorFactory, ValidationCollector<?> collector) {
        this.validatorFactory = validatorFactory;
        this.collector = collector;
    }

    public SchemaParsingContext parseRootSchema(String baseUri, JsonNode node) {
        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(baseUri);
            ctx.registerSchema(baseUri, Schema.getBooleanSchema(node.asBoolean()).asIdentifiableSchema(URI.create(baseUri)));
            return ctx;
        } else {
            Map<String, JsonNode> objectMap = node.asObject();
            String id = Optional.ofNullable(objectMap.get("$id"))
                    .map(JsonNode::asString)
                    .orElse(baseUri);
            SchemaParsingContext ctx = new SchemaParsingContext(id);
            List<Validator> validators = parseValidators(ctx, objectMap);
            ctx.registerSchema(id, new IdentifiableSchema(URI.create(id), validators));
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
        ctx.registerSchema(ctx.getAbsoluteUri(node), Schema.getBooleanSchema(node.asBoolean()));
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        parseObject(ctx, node, ctx.getAbsoluteUri(node));
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node, String id) {
        Map<String, JsonNode> objectMap = node.asObject();
        List<Validator> validators = parseValidators(ctx, objectMap);
        if (objectMap.containsKey("$id")) {
            URI baseUri = URI.create(ctx.getBaseUri()).resolve(objectMap.get("$id").asString());
            ctx.registerSchema(baseUri.toString(), new IdentifiableSchema(baseUri, validators));
        } else {
            ctx.registerSchema(id, new Schema(validators));
        }
    }

    private List<Validator> parseValidators(SchemaParsingContext ctx, Map<String, JsonNode> object) {
        SchemaParsingContext newCtx = ctx.withCurrentSchemaContext(object);
        List<Validator> validators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            validatorFactory.fromField(newCtx, entry.getKey(), entry.getValue())
                    .map(validator -> new ReportingValidator(collector, entry.getValue(), validator))
                    .ifPresent(validators::add);
            parseNode(newCtx, entry.getValue());
        }
        return validators;
    }
}

