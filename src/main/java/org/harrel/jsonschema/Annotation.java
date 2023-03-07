package org.harrel.jsonschema;

public record Annotation(String schemaPath, String instancePath, String message, boolean successful) {}
