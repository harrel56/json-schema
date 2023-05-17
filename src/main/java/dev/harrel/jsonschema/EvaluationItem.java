package dev.harrel.jsonschema;

class EvaluationItem implements Annotation, Error {
    private final String evaluationPath;
    private final String schemaLocation;
    private final String instanceLocation;
    private final String keyword;
    private final boolean valid;
    private final Object annotation;
    private final String error;

    public EvaluationItem(String evaluationPath,
                          String schemaLocation,
                          String instanceLocation,
                          String keyword,
                          boolean valid,
                          Object annotation,
                          String error) {
        this.evaluationPath = evaluationPath;
        this.schemaLocation = schemaLocation;
        this.instanceLocation = instanceLocation;
        this.keyword = keyword;
        this.valid = valid;
        this.annotation = annotation;
        this.error = error;
    }

    @Override
    public String getEvaluationPath() {
        return evaluationPath;
    }

    @Override
    public String getSchemaLocation() {
        return schemaLocation;
    }

    @Override
    public String getInstanceLocation() {
        return instanceLocation;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public Object getAnnotation() {
        return annotation;
    }

    @Override
    public String getError() {
        return error;
    }

    public boolean isValid() {
        return valid;
    }
}