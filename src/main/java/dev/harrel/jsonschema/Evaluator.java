package dev.harrel.jsonschema;

import java.util.Set;
import java.util.function.Supplier;

/**
 * {@code Evaluator} interface is the main abstraction for the keyword evaluation logic.
 */
public interface Evaluator {
    /**
     * Evaluation logic for a specific keyword.
     * Must not throw any exceptions, any possible evaluation failures should be reflected by returned object.
     *
     * @param ctx  current evaluation context
     * @param node JSON node on which the evaluation should act upon
     * @return evaluation result
     * @see Result
     */
    Result evaluate(EvaluationContext ctx, JsonNode node);

    /**
     * Order of {@code Evaluator} determines order of evaluators execution in scope of single schema location.
     * By default, evaluators are executed in order of their occurrence in JSON object. If {@code Evaluator}
     * is required to be run before or after other evaluators, manipulating order value is the only way of achieving
     * this behaviour.
     *
     * @return order
     */
    default int getOrder() {
        return 0;
    }

    /**
     * If evaluator is considered to belong to some specific vocabularies, then it should return their URIs. By default,
     * this method returns an empty set, which means it belongs to no vocabulary, and it will always be taken into
     * consideration when validating against a schema.
     * At least one of returned vocabulary URIs have to be "active" for evaluator to be run.
     * @return set of vocabulary URIs
     * @deprecated since version 1.7.0, subject to removal in future releases.
     * Throws {@code UnsupportedOperationException}. Deciding whether this evaluator is turned on or off
     * should be done now in {@link EvaluatorFactory} code.
     */
    @Deprecated
    default Set<String> getVocabularies() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@code Result} class represents evaluation outcome.
     */
    final class Result {
        private static final Result SUCCESSFUL_RESULT = new Result(true, null, null);
        private static final Result FAILED_RESULT = new Result(false, null, () -> null);

        private final boolean valid;
        private final Object annotation;
        private final Supplier<String> errorSupplier;

        private Result(boolean valid, Object annotation, Supplier<String> errorSupplier) {
            this.valid = valid;
            this.annotation = annotation;
            this.errorSupplier = errorSupplier;
        }

        /**
         * Factory method for successful evaluation result.
         *
         * @return {@link Result}
         */
        public static Result success() {
            return SUCCESSFUL_RESULT;
        }

        /**
         * Factory method for successful evaluation result with annotation.
         *
         * @return {@link Result}
         */
        public static Result success(Object annotation) {
            return new Result(true, annotation, null);
        }

        /**
         * Factory method for failed evaluation result.
         *
         * @return {@link Result}
         */
        public static Result failure() {
            return FAILED_RESULT;
        }

        /**
         * Factory method for failed evaluation result with message.
         *
         * @return {@link Result}
         */
        public static Result failure(String message) {
            return new Result(false, null, () -> message);
        }

        /**
         * Lazy error message construction is internal for now.
         */
        static Result failure(Supplier<String> messageSupplier) {
            return new Result(false, null, messageSupplier);
        }

        boolean isValid() {
            return valid;
        }

        Object getAnnotation() {
            return annotation;
        }

        Supplier<String> getErrorSupplier() {
            return errorSupplier;
        }
    }
}
