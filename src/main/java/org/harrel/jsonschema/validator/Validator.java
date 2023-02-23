package org.harrel.jsonschema.validator;

import org.harrel.jsonschema.JsonNode;
import org.harrel.jsonschema.ValidationContext;

public interface Validator extends Comparable<Validator> {
    ValidationResult validate(ValidationContext ctx, JsonNode node);

    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(Validator other) {
        return Integer.compare(getOrder(), other.getOrder());
    }
}
