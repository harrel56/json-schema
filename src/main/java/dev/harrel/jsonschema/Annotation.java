package dev.harrel.jsonschema;

/**
 * {@code Annotation} class represents collected annotation.
 */
public class Annotation extends EvaluationItem {
    private final Object annotation;

    Annotation(String evaluationPath, String schemaLocation, String instanceLocation, String keyword, Object annotation) {
        super(evaluationPath, schemaLocation, instanceLocation, keyword);
        this.annotation = annotation;
    }

    /**
     * Returns collected annotation.
     */
    public Object getAnnotation() {
        return annotation;
    }
}
