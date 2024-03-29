package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@code Dialect} interface is the main abstraction for a specific (possibly custom) JSON Schema dialects.
 */
public interface Dialect {
    /**
     * Specification version used by this dialect.
     * Different versions are basically a modes in which validator can run in.
     *
     * @return specification version
     */
    SpecificationVersion getSpecificationVersion();

    /**
     * Optional meta-schema URI against which all schemas will be validated (a <i>$schema</i> keyword overrides it).
     * If empty, no validation will be performed by default.
     * @apiNote Default implementation returns {@code getSpecificationVersion().getId()}.
     *
     * @return optional with meta-schema URI
     */
    default Optional<URI> getMetaSchemaUri() {
        return Optional.of(getSpecificationVersion().getId());
    }

    /**
     * Core evaluator factory used by this dialect.
     *
     * @return evaluator factory
     */
    EvaluatorFactory getEvaluatorFactory();

    /**
     * All vocabulary URIs that are supported by this dialect. Actual implementation for vocabularies and keywords
     * should be provided by {@link Dialect#getEvaluatorFactory()}
     *
     * @return set of supported vocabularies
     */
    Set<String> getSupportedVocabularies();

    /**
     * All vocabulary URIs that are considered required at all times. If <i>$vocabulary</i> keyword
     * does not include this vocabulary or sets it as optional, {@link VocabularyException} will be thrown.
     *
     * @return set of required vocabularies
     */
    Set<String> getRequiredVocabularies();

    /**
     * Default <i>$vocabulary</i> keyword contents for this dialect. If meta-schema does not include
     * <i>$vocabulary</i> keyword this default will be used.
     *
     * @return default vocabulary object
     */
    Map<String, Boolean> getDefaultVocabularyObject();
}
