package org.harrel.jsonschema;

import java.util.*;
import java.util.function.BiFunction;

public class ValidatorFactory {

    private final Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> validatorsMap;

    public ValidatorFactory() {
        Map<String, BiFunction<SchemaParsingContext, JsonNode, Validator>> map = new HashMap<>();
        map.put("$ref", RefValidator::new);
        map.put("anyOf", AnyOfValidator::new);
        map.put("properties", PropertiesValidator::new);
        map.put("type", (ctx, node) -> TypeChecks.getTypeCheck(node));
//        map.put("const", (parsingCtx, node) -> ((ctx, n) -> node.isEqualTo(n)));
        this.validatorsMap = Collections.unmodifiableMap(map);
    }

    public Optional<Validator> fromField(SchemaParsingContext ctx, String fieldName, JsonNode node) {
        return Optional.ofNullable(validatorsMap.get(fieldName))
                .map(fun -> fun.apply(ctx, node));
    }

    static class RefValidator implements Validator {
        private final String ref;

        RefValidator(SchemaParsingContext ctx, JsonNode node) {
            this.ref = node.asString();
        }

        @Override
        public ValidationResult validate(ValidationContext ctx, JsonNode node) {
            Optional<Schema> schema = ctx.resolveSchema(ref);
            if (schema.isEmpty()) {
                return Result.failure("Resolution of $ref (%s) failed".formatted(ref));
            } else {
                return schema.get().validate(ctx, node) ? Result.success() : Result.failure("Referenced schema validation failed.");
            }
        }
    }

    static class AnyOfValidator implements Validator {
        private final List<String> jsonPointers;

        AnyOfValidator(SchemaParsingContext ctx, JsonNode node) {
            List<String> pointersTemp = new ArrayList<>();
            for (JsonNode element : node.asArray()) {
                pointersTemp.add(ctx.getAbsoluteUri(element));
            }
            this.jsonPointers = Collections.unmodifiableList(pointersTemp);
        }

        @Override
        public ValidationResult validate(ValidationContext ctx, JsonNode node) {
            boolean invalid = jsonPointers.stream()
                    .noneMatch(uri -> ctx.resolveRequiredSchema(uri).validate(ctx, node));
            return invalid ? Result.failure("None of the schemas matched.") : Result.success();
        }
    }

    static class PropertiesValidator implements Validator {
        private final Map<String, String> jsonPointerMap;

        PropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
            Map<String, String> uris = new HashMap<>();
            for (Map.Entry<String, JsonNode> entry : node.asObject()) {
                uris.put(entry.getKey(), ctx.getAbsoluteUri(entry.getValue()));
            }
            this.jsonPointerMap = Collections.unmodifiableMap(uris);
        }

        @Override
        public ValidationResult validate(ValidationContext ctx, JsonNode node) {
            if (!node.isObject()) {
                return Result.success();
            }

            boolean valid = true;
            for (Map.Entry<String, JsonNode> entry : node.asObject()) {
                String schemaUri = jsonPointerMap.get(entry.getKey());
                if (schemaUri != null) {
                    valid = valid && ctx.resolveRequiredSchema(schemaUri).validate(ctx, entry.getValue());
                }
            }
            return valid ? Result.success() : Result.failure("Properties validation failed.");
        }
    }
}
