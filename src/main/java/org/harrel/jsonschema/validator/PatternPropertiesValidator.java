package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PatternPropertiesValidator extends BasicValidator {
    private final Map<Pattern, String> schemasByPatterns;

    PatternPropertiesValidator(SchemaParsingContext ctx, JsonNode node) {
        super("PatternProperties validation failed.");
        this.schemasByPatterns = node.asObject().entrySet().stream()
                .collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), e -> ctx.getAbsoluteUri(e.getValue())));
    }

    @Override
    protected boolean doValidate(ValidationContext ctx, JsonNode node) {
        if (!node.isObject()) {
            return true;
        }

        boolean valid = true;
        for (Map.Entry<String, JsonNode> entry : node.asObject().entrySet()) {
            List<Schema> schemas = schemasByPatterns.entrySet().stream()
                    .filter(e -> e.getKey().matcher(entry.getKey()).find())
                    .map(Map.Entry::getValue)
                    .map(ctx::resolveRequiredSchema)
                    .toList();
            valid = StreamUtil.exhaustiveAllMatch(schemas.stream(), schema -> schema.validate(ctx, entry.getValue())) && valid;
        }
        return valid;
    }
}
