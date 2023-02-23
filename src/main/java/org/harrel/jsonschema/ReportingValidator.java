package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

class ReportingValidator implements Validator {
    private final AnnotationCollector<?> collector;
    private final JsonNode schemaNode;
    private final Validator validator;

    public ReportingValidator(AnnotationCollector<?> collector, JsonNode schemaNode, Validator validator) {
        this.collector = collector;
        this.schemaNode = schemaNode;
        this.validator = validator;
    }

    public ValidationResult validate(ValidationContext ctx, JsonNode instanceNode) {
        ValidationResult result = validator.validate(ctx, instanceNode);
        if (result.isValid()) {
            collector.onSuccess(ctx, schemaNode, instanceNode);
        } else {
            collector.onFailure(ctx, schemaNode, instanceNode, result.getErrorMessage());
        }
        return result;
    }
}
