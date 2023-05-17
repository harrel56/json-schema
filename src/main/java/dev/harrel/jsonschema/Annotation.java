package dev.harrel.jsonschema;

/**
 * {@code Annotation} interface represents collected annotation.
 */
public interface Annotation extends ResultItem {
    /**
     * Returns collected annotation.
     */
    Object getAnnotation();
}
