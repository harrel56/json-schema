package dev.harrel.jsonschema;

/**
 * {@code Error} interface represents validation error.
 */
public interface Error extends ResultItem {
    /**
     * Returns validation error.
     */
    String getError();
}
