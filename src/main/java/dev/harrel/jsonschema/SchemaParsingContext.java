package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * {@code SchemaParsingContext} class represents state of current schema parsing process.
 * {@link EvaluatorFactory} can use this class for evaluator creation process.
 *
 * @see EvaluatorFactory
 */
public final class SchemaParsingContext {
    private final MetaValidationData metaValidationData;
    private final URI baseUri;
    private final URI parentUri; // todo this should contain more metadata: $schema value, explicit or one assumed from parent
    private final SchemaRegistry schemaRegistry;
    private final Map<String, JsonNode> currentSchemaObject;

    private SchemaParsingContext(MetaValidationData metaValidationData, URI baseUri, URI parentUri, SchemaRegistry schemaRegistry, Map<String, JsonNode> currentSchemaObject) {
        this.metaValidationData = metaValidationData;
        this.baseUri = baseUri;
        this.parentUri = parentUri;
        this.schemaRegistry = schemaRegistry;
        this.currentSchemaObject = currentSchemaObject;
    }

    SchemaParsingContext(MetaValidationData metaValidationData, SchemaRegistry schemaRegistry, URI baseUri, Map<String, JsonNode> currentSchemaObject) {
        this(metaValidationData, baseUri, baseUri, schemaRegistry, currentSchemaObject);
    }

    SchemaParsingContext forChild(MetaValidationData metaValidationData, Map<String, JsonNode> currentSchemaObject, URI parentUri) {
        return new SchemaParsingContext(metaValidationData, baseUri, parentUri, schemaRegistry, currentSchemaObject);
    }

    SchemaParsingContext forChild(MetaValidationData metaValidationData, Map<String, JsonNode> currentSchemaObject) {
        return forChild(metaValidationData, currentSchemaObject, parentUri);
    }

    MetaValidationData getMetaValidationData() {
        return metaValidationData;
    }

    //    Map<String, Boolean> getVocabulariesObject() {
//        return JsonNodeUtil.getVocabulariesObject(currentSchemaObject).orElse(metaValidationData.getDefaultVocabularyObject());
//    }

    URI getBaseUri() {
        return baseUri;
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
        return baseUri + "#" + jsonPointer;
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
        return new CompoundUri(baseUri, node.getJsonPointer());
    }
}
