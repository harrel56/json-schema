package org.harrel.jsonschema;

import java.net.URI;
import java.util.*;

class JsonParser {
    private final JsonNodeFactory jsonNodeFactory;
    private final CoreValidatorFactory validatorFactory;
    private final SchemaRegistry schemaRegistry;

    JsonParser(JsonNodeFactory jsonNodeFactory, CoreValidatorFactory validatorFactory, SchemaRegistry schemaRegistry) {
        this.jsonNodeFactory = jsonNodeFactory;
        this.validatorFactory = validatorFactory;
        this.schemaRegistry = schemaRegistry;
    }

    void parseRootSchema(String baseUri, String rawJson) {
        parseRootSchema(URI.create(baseUri), jsonNodeFactory.create(rawJson));
    }

    URI parseRootSchema(URI baseUri, JsonNode node) {
        if (node.isBoolean()) {
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            boolean schemaValue = node.asBoolean();
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, List.of(new ValidatorWrapper(String.valueOf(schemaValue), node, Schema.getBooleanValidator(schemaValue))));
            return baseUri;
        } else {
            Map<String, JsonNode> objectMap = node.asObject();
            JsonNode idNode = objectMap.get("$id");
            if (idNode != null && idNode.isString()) {
                String idString = idNode.asString();
                SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, idString);
                List<ValidatorWrapper> validators = parseValidators(ctx, objectMap);
                schemaRegistry.registerIdentifiableSchema(ctx, URI.create(idString), node, validators);
            }
            SchemaParsingContext ctx = new SchemaParsingContext(schemaRegistry, baseUri.toString());
            List<ValidatorWrapper> validators = parseValidators(ctx, objectMap);
            schemaRegistry.registerIdentifiableSchema(ctx, baseUri, node, validators);

            return Optional.ofNullable(objectMap.get("$id"))
                    .map(JsonNode::asString)
                    .map(URI::create)
                    .orElse(baseUri);
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
        boolean schemaValue = node.asBoolean();
        Validator booleanValidator = Schema.getBooleanValidator(schemaValue);
        schemaRegistry.registerSchema(ctx, node, List.of(new ValidatorWrapper(String.valueOf(schemaValue), node, booleanValidator)));
    }

    private void parseArray(SchemaParsingContext ctx, JsonNode node) {
        for (JsonNode element : node.asArray()) {
            parseNode(ctx, element);
        }
    }

    private void parseObject(SchemaParsingContext ctx, JsonNode node) {
        Map<String, JsonNode> objectMap = node.asObject();
        JsonNode idNode = objectMap.get("$id");
        if (idNode != null && idNode.isString()) {
            URI uri = ctx.getParentUri().resolve(idNode.asString());
            SchemaParsingContext newCtx = ctx.withParentUri(uri);
            List<ValidatorWrapper> validators = parseValidators(newCtx, objectMap);
            schemaRegistry.registerIdentifiableSchema(newCtx, uri, node, validators);
        } else {
            schemaRegistry.registerSchema(ctx, node, parseValidators(ctx, objectMap));
        }
    }

    private List<ValidatorWrapper> parseValidators(SchemaParsingContext ctx, Map<String, JsonNode> object) {
        SchemaParsingContext newCtx = ctx.withCurrentSchemaContext(object);
        List<ValidatorWrapper> validators = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : object.entrySet()) {
            validatorFactory.create(newCtx, entry.getKey(), entry.getValue())
                    .map(validator -> new ValidatorWrapper(entry.getKey(), entry.getValue(), validator))
                    .ifPresent(validators::add);
            parseNode(newCtx, entry.getValue());
        }
        return validators;
    }
}

