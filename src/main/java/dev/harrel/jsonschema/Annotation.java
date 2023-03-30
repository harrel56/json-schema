package dev.harrel.jsonschema;

public record Annotation(AnnotationHeader header,
                         String keyword,
                         String message,
                         boolean successful) {}
