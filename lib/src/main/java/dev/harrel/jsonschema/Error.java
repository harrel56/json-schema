package dev.harrel.jsonschema;

/**
 * {@code Error} class represents validation error.
 */
public class Error extends EvaluationItem {
    private final String error;

    Error(String evaluationPath, String schemaLocation, String instanceLocation, String keyword, String error) {
        super(evaluationPath, schemaLocation, instanceLocation, keyword);
        this.error = error;
    }

    /**
     * Returns validation error.
     */
    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Error{" + super.toString() + ", error=" + error + "}";
    }
}
