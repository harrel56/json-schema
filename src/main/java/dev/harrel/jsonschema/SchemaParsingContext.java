package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

/**
 * {@code SchemaParsingContext} class represents state of current schema parsing process.
 * {@link EvaluatorFactory} can use this class for evaluator creation process.
 * @see EvaluatorFactory
 */
public final class SchemaParsingContext {
    private final URI baseUri;
    private final URI parentUri;
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(URI baseUri, URI parentUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        this.baseUri = baseUri;
        this.parentUri = parentUri;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
    }

    SchemaParsingContext(SchemaRegistry schemaRegistry, String baseUri) {
        this(URI.create(baseUri), URI.create(baseUri), schemaRegistry, Map.of());
    }

    SchemaParsingContext withParentUri(URI parentUri) {
        return new SchemaParsingContext(baseUri, parentUri, schemaRegistry, currentSchemaObject);
    }

    SchemaParsingContext withCurrentSchemaContext(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(baseUri, parentUri, schemaRegistry, Collections.unmodifiableMap(currentSchemaObject));
    }

    /**
     * Returns URI of the closest parent schema that contains <i>$id</i> keyword.
     * If there is no such parent, then the URI of root schema is returned.
     */
    public URI getParentUri() {
        return parentUri;
    }

    /**
     * Calculates absolute URI to the provided {@code JsonNode}.
     * @see SchemaParsingContext#getAbsoluteUri(String)
     */
    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    /**
     * Calculates absolute URI for the corresponding JSON pointer.
     * @param jsonPointer JSON pointer string, can be prefixed with "#"
     * @return absolute URI
     */
    public String getAbsoluteUri(String jsonPointer) {
        if (jsonPointer.isEmpty()) {
            return baseUri + "#";
        } else if (jsonPointer.startsWith("#")) {
            return baseUri + jsonPointer;
        } else {
            return baseUri + "#" + jsonPointer;
        }
    }

    /**
     * Returns JSON object which is currently being parsed in form of map.
     * @return unmodifiable map representing schema object
     */
    public Map<String, JsonNode> getCurrentSchemaObject() {
        return Collections.unmodifiableMap(currentSchemaObject);
    }
}
