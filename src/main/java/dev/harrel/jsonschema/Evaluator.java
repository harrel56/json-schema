package dev.harrel.jsonschema;

import java.util.Objects;
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
        private static final Result SUCCESSFUL_RESULT = new Result(true, null, null, null);
        private static final Result FAILED_RESULT = new Result(false, null, null, null);

        private final boolean valid;
        private final Object annotation;
        /* Either bundle key or hardcoded message */
        private final String error;
        private final Supplier<Object[]> argsSupplier;

        private Result(boolean valid, Object annotation, String error, Supplier<Object[]> argsSupplier) {
            this.valid = valid;
            this.annotation = annotation;
            this.error = error;
            this.argsSupplier = argsSupplier;
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
            return new Result(true, annotation, null, null);
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
         * Factory method for failed evaluation result with a hardcoded message.
         *
         * @return {@link Result}
         */
        public static Result failure(String message) {
            return new Result(false, null, message, null);
        }

        /**
         * Factory method for failed evaluation result with a message looked up by the provided key.
         * See {@link MessageProvider}.
         *
         * @param key key value under which the message template can be found
         * @param args arguments which will be used to format the message
         * @return {@link Result}
         */
        public static Result formattedFailure(String key, Object... args) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(args);
            return new Result(false, null, key, () -> args);
        }

        /**
         * Lazy error message construction is internal for now.
         */
        static Result formattedFailure(String key, Supplier<Object[]> argsSupplier) {
            Objects.requireNonNull(argsSupplier);
            return new Result(false, null, key, argsSupplier);
        }

        /**
         * Just for unevaluated vocabulary so it sees unevaluated items properly even on errors.
         */
        static Result annotatedFailure(Object annotation) {
            return new Result(false, annotation, null, null);
        }

        boolean isValid() {
            return valid;
        }

        Object getAnnotation() {
            return annotation;
        }

        String getError() {
            return error;
        }

        Supplier<Object[]> getArgsSupplier() {
            return argsSupplier;
        }
    }
}
