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
    private final Map<String, JsonNode> currentSchemaObject;
    private final Deque<URI> uriStack;

    private SchemaParsingContext(MetaSchemaData metaSchemaData, Map<String, JsonNode> currentSchemaObject, Deque<URI> uriStack) {
        this.metaSchemaData = Objects.requireNonNull(metaSchemaData);
        this.currentSchemaObject = Objects.requireNonNull(currentSchemaObject);
        this.uriStack = Objects.requireNonNull(uriStack);
    }

    SchemaParsingContext(MetaSchemaData metaSchemaData, URI baseUri, Map<String, JsonNode> currentSchemaObject) {
        this(metaSchemaData, currentSchemaObject, new ArrayDeque<>(Collections.singletonList(baseUri)));
    }

    SchemaParsingContext forChild(MetaSchemaData metaSchemaData, Map<String, JsonNode> currentSchemaObject, URI parentUri) {
        ArrayDeque<URI> newUriStack = new ArrayDeque<>(uriStack);
        newUriStack.push(parentUri);
        return new SchemaParsingContext(metaSchemaData, currentSchemaObject, newUriStack);
    }

    SchemaParsingContext forChild(Map<String, JsonNode> currentSchemaObject) {
        return new SchemaParsingContext(metaSchemaData, currentSchemaObject, uriStack);
    }

    MetaSchemaData getMetaSchemaData() {
        return metaSchemaData;
    }

    URI getGrandparentUri() {
        Iterator<URI> it = uriStack.iterator();
        URI parent = it.next();
        if (it.hasNext()) {
            return it.next();
        } else {
            return parent;
        }
    }

    URI getBaseUri() {
        return uriStack.getLast();
    }

    CompoundUri getCompoundUri(JsonNode node) {
        return new CompoundUri(getBaseUri(), node.getJsonPointer());
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

    /**
     * Returns the dialect in which the schema is being parsed.
     *
     * @return current dialect
     */
    public Dialect getDialect() {
        return metaSchemaData.dialect;
    }

    /**
     * Returns currently active vocabularies.
     *
     * @return set of vocabularies (never null)
     */
    public Set<String> getActiveVocabularies() {
        return Collections.unmodifiableSet(metaSchemaData.activeVocabularies);
    }
}
