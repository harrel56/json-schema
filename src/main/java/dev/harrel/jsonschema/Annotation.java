package dev.harrel.jsonschema;

public record Annotation(String evaluationPath,
                         String schemaLocation,
                         String instanceLocation,
                         String keyword,
                         String message,
                         boolean valid) {}
