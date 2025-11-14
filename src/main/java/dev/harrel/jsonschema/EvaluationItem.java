package dev.harrel.jsonschema;

import java.util.function.Supplier;

class EvaluationItem {
    private final String evaluationPath;
    private final String schemaLocation;
    private final String instanceLocation;
    private final String keyword;

    EvaluationItem(String evaluationPath,
                   String schemaLocation,
                   String instanceLocation,
                   String keyword) {
        this.evaluationPath = evaluationPath;
        this.schemaLocation = schemaLocation;
        this.instanceLocation = instanceLocation;
        this.keyword = keyword;
    }

    /**
     * Returns JSON pointer like path representing evaluation point in schema JSON.
     */
    public String getEvaluationPath() {
        return evaluationPath;
    }

    /**
     * Returns absolute schema location which uniquely identifies given schema.
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * Returns JSON pointer like path representing evaluation point in instance JSON.
     */
    public String getInstanceLocation() {
        return instanceLocation;
    }

    /**
     * Returns keyword name associated with given evaluation point. Might be null.
     */
    public String getKeyword() {
        return keyword;
    }

    @Override
    public String toString() {
        return "keyword=" + keyword +
                ", evaluationPath=" + evaluationPath +
                ", schemaLocation=" + schemaLocation +
                ", instanceLocation=" + instanceLocation;
    }
}

final class RawError extends EvaluationItem {
    final String error;
    final Supplier<Object[]> argsSupplier;

    RawError(String evaluationPath, String schemaLocation, String instanceLocation, String keyword, String error, Supplier<Object[]> argsSupplier) {
        super(evaluationPath, schemaLocation, instanceLocation, keyword);
        this.error = error;
        this.argsSupplier = argsSupplier;
    }
}