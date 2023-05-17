package dev.harrel.jsonschema;

/**
 * {@code ResultItem} interface provides methods which can uniquely identify specific point
 * in validation flow.
 */
public interface ResultItem {
    /**
     * Returns JSON pointer like path representing evaluation point in schema JSON.
     */
    String getEvaluationPath();

    /**
     * Returns absolute schema location which uniquely identifies given schema.
     */
    String getSchemaLocation();

    /**
     * Returns JSON pointer like path representing evaluation point in instance JSON.
     */
    String getInstanceLocation();

    /**
     * Returns keyword name associated with given evaluation point. Might be null.
     */
    String getKeyword();
}
