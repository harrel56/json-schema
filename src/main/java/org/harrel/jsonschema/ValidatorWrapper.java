package org.harrel.jsonschema;

class ValidatorWrapper implements Validator {
    private final String keyword;
    private final String keywordPath;
    private final Validator validator;

    ValidatorWrapper(String keyword, JsonNode keywordNode, Validator validator) {
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

    String getKeyword() {
        return keyword;
    }

    String getKeywordPath() {
        return keywordPath;
    }
}
