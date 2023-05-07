package dev.harrel.jsonschema;

/**
 * {@code Evaluator} interface is the main abstraction for the keyword evaluation logic.
 */
public interface Evaluator {
    /**
     * Evaluation logic for a specific keyword.
     * Must not throw any exceptions, any possible evaluation failures should be reflected by returned object.
     * @param ctx current evaluation context
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
     * @return order
     */
    default int getOrder() {
        return 0;
    }

    /**
     * {@code Result} class represents evaluation outcome.
     */
    final class Result {
        private static final Result SUCCESSFUL_RESULT = new Result(true, null);
        private static final Result FAILED_RESULT = new Result(false, null);

        private final boolean valid;
        private final String errorMessage;

        private Result(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        /**
         * Factory method for successful evaluation result.
         * @return {@link Result}
         */
        public static Result success() {
            return SUCCESSFUL_RESULT;
        }

        /**
         * Factory method for failed evaluation result.
         * @return {@link Result}
         */
        public static Result failure() {
            return FAILED_RESULT;
        }

        /**
         * Factory method for failed evaluation result with message.
         * @return {@link Result}
         */
        public static Result failure(String message) {
            return new Result(false, message);
        }

        boolean isValid() {
            return valid;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
