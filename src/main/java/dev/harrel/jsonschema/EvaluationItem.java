package dev.harrel.jsonschema;

public record EvaluationItem(String evaluationPath,
                             String schemaLocation,
                             String instanceLocation,
                             String keyword,
                             boolean valid,
                             Object annotation,
                             String error) {}
