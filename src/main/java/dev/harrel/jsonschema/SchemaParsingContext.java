package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static java.util.Collections.unmodifiableMap;

/**
 * {@code SchemaParsingContext} class represents state of current schema parsing process.
 * {@link EvaluatorFactory} can use this class for evaluator creation process.
 *
 * @see EvaluatorFactory
 */
public final class SchemaParsingContext {
    private final MetaSchemaData metaSchemaData;
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;
    private final LinkedList<URI> uriStack;

    private SchemaParsingContext(MetaSchemaData metaSchemaData, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject, LinkedList<URI> uriStack) {
        this.metaSchemaData = metaSchemaData;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
        this.uriStack = uriStack;
    }

    SchemaParsingContext(MetaSchemaData metaSchemaData, URI baseUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        this(metaSchemaData, schemaRegistry, currentSchemaObject, new LinkedList<>(Collections.singletonList(baseUri)));
    }

    SchemaParsingContext forChild(MetaSchemaData metaSchemaData, Map<String, JsonNode> currentSchemaObject, URI parentUri) {
        LinkedList<URI> newUriStack = new LinkedList<>(uriStack);
        newUriStack.push(parentUri);
        return new SchemaParsingContext(metaSchemaData, schemaRegistry, currentSchemaObject, newUriStack);
    }

    SchemaParsingContext forChild(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(metaSchemaData, schemaRegistry, currentSchemaObject, uriStack);
    }

    MetaSchemaData getMetaValidationData() {
        return metaSchemaData;
    }

    SpecificationVersion getSpecificationVersion() {
        return metaSchemaData.dialect.getSpecificationVersion();
    }

    LinkedList<URI> getUriStack() {
        return uriStack;
    }

    URI getBaseUri() {
        return uriStack.getLast();
    }

    /**
     * Returns URI of the closest parent schema that contains <i>$id</i> keyword.
     * If there is no such parent, then the URI of root schema is returned.
     */
    public URI getParentUri() {
        return uriStack.getFirst();
    }

    /**
     * Calculates absolute URI to the provided {@code JsonNode}.
     *
     * @see SchemaParsingContext#getAbsoluteUri(String)
     */
    public String getAbsoluteUri(JsonNode node) {
        return getAbsoluteUri(node.getJsonPointer());
    }

    /**
     * Calculates absolute URI for the corresponding JSON pointer.
     *
     * @param jsonPointer JSON pointer string
     * @return absolute URI
     */
    public String getAbsoluteUri(String jsonPointer) {
        return getBaseUri() + "#" + jsonPointer;
    }

    /**
     * Returns JSON object which is currently being parsed in form of map.
     *
     * @return unmodifiable map representing schema object
     */
    public Map<String, JsonNode> getCurrentSchemaObject() {
        return unmodifiableMap(currentSchemaObject);
    }

    CompoundUri getCompoundUri(JsonNode node) {
        return new CompoundUri(getBaseUri(), node.getJsonPointer());
    }
}
