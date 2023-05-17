package dev.harrel.jsonschema;

record EvaluationItem(String evaluationPath,
                             String schemaLocation,
                             String instanceLocation,
                             String keyword,
                             boolean valid,
                             Object annotation,
                             String error) implements Annotation, Error {
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