package org.harrel.jsonschema;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private final ValidatorFactory validatorFactory;
    private final ValidationCollector<?> collector;

    public JsonParser(ValidatorFactory validatorFactory, ValidationCollector<?> collector) {
        this.validatorFactory = validatorFactory;
        this.collector = collector;
    }

    public SchemaParsingContext parse(URI baseUri, JsonNode node) {
        SchemaParsingContext ctx = new SchemaParsingContext(baseUri);
        if (node.isBoolean()) {
            parseBoolean(ctx, node);
        } else {
            parseObject(ctx, node);
        }
        return ctx;
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
        List<Validator> validators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : node.asObject()) {
            validatorFactory.fromField(ctx, entry.getKey(), entry.getValue())
                    .map(validator -> new ReportingValidator(collector, entry.getValue(), validator))
                    .ifPresent(validators::add);
            parseNode(ctx, entry.getValue());
        }
        ctx.registerSchema(ctx.getAbsoluteUri(node), new Schema(validators));
    }
}

