package org.harrel.jsonschema;

import org.harrel.jsonschema.validator.ValidationResult;
import org.harrel.jsonschema.validator.Validator;

class ValidatorWrapper implements Validator {
    private final String keyword;
    private final String keywordPath;
    private final Validator validator;

    public ValidatorWrapper(String keyword, JsonNode keywordNode, Validator validator) {
        this.keyword = keyword;
        this.keywordPath = keywordNode.getJsonPointer();
        this.validator = validator;
    }

    @Override
    public ValidationResult validate(ValidationContext ctx, JsonNode instanceNode) {
        return validator.validate(ctx, instanceNode);
    }

    @Override
    public int getOrder() {
        return validator.getOrder();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getKeywordPath() {
        return keywordPath;
    }
}
