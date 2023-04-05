package dev.harrel.jsonschema;

import java.util.Objects;

final class ValidatorWrapper implements Validator {
    private final String keyword;
    private final String keywordPath;
    private final Validator validator;

    ValidatorWrapper(String keyword, String keywordPath, Validator validator) {
        this.keyword = Objects.requireNonNull(keyword);
        this.keywordPath = Objects.requireNonNull(keywordPath);
        this.validator = Objects.requireNonNull(validator);
    }

    ValidatorWrapper(String keyword, JsonNode keywordNode, Validator validator) {
        this(keyword, keywordNode.getJsonPointer(), validator);
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
