package org.harrel.jsonschema;

import java.util.Optional;

public interface ValidatorFactory {
    Optional<Validator> create(SchemaParsingContext context, String fieldName, JsonNode fieldNode);
}
