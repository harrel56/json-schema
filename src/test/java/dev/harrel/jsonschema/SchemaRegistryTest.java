package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class SchemaRegistryTest {

    @Test
    void shouldRestoreStateProperly() {
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        SchemaParsingContext ctx = new SchemaParsingContext(new Dialects.Draft2020Dialect(), schemaRegistry, URI.create("urn:test"), emptyMap());
        JacksonNode.Factory factory = new JacksonNode.Factory();
        JacksonNode rootSchemaNode = factory.create("""
                {
                  "properties": {
                    "field": true
                  }
                }""");
        JsonNode subSchemaNode = rootSchemaNode.asObject().get("properties").asObject().get("field");

        schemaRegistry.registerSchema(ctx, subSchemaNode, List.of(), Set.of());
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#/properties/field"))).isNotNull();

        SchemaRegistry.State snapshot = schemaRegistry.createSnapshot();
        schemaRegistry.registerSchema(ctx, rootSchemaNode, List.of(), Set.of());
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#"))).isNotNull();
        SchemaRegistry.State nextSnapshot = schemaRegistry.createSnapshot();

        schemaRegistry.restoreSnapshot(snapshot);
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#/properties/field"))).isNotNull();
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#"))).isNull();

        schemaRegistry.restoreSnapshot(nextSnapshot);
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#/properties/field"))).isNotNull();
        assertThat(schemaRegistry.get(CompoundUri.fromString("urn:test#"))).isNotNull();
    }
}