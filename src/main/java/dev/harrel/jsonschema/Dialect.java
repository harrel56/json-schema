package dev.harrel.jsonschema;

import java.util.Map;
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
     * Meta-schema URI against which all schemas will be validated,
     * unless there is a <i>$schema</i> keyword present.
     *
     * @return meta-schema URI
     */
    String getMetaSchema();

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
     * @deprecated since version 1.7.0. As handling dialects and spec versions has changed this method is no longer used.
     * Implementing it has no effect.
     *
     * @return set of supported vocabularies
     */
    @Deprecated
    default Set<String> getSupportedVocabularies() {
        throw new UnsupportedOperationException();
    }

    /**
     * All vocabulary URIs that are considered required at all times. If <i>$vocabulary</i> keyword
     * does not include this vocabulary or sets it as optional, {@link VocabularyException} will be thrown.
     *
     * @deprecated since version 1.7.0. As handling dialects and spec versions has changed this method is no longer used.
     * Implementing it has no effect.
     *
     * @return set of required vocabularies
     */
    @Deprecated
    default Set<String> getRequiredVocabularies() {
        throw new UnsupportedOperationException();
    }

    /**
     * Default <i>$vocabulary</i> keyword contents for this dialect. If meta-schema does not include
     * <i>$vocabulary</i> keyword this default will be used.
     *
     * @deprecated since version 1.7.0. As handling dialects and spec versions has changed this method is no longer used.
     * Implementing it has no effect.
     *
     * @return default vocabulary object
     */
    @Deprecated
    default Map<String, Boolean> getDefaultVocabularyObject() {
        throw new UnsupportedOperationException();
    }
}
