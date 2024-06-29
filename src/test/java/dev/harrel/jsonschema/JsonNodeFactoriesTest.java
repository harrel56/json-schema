package dev.harrel.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.harrel.jsonschema.providers.JacksonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JsonNodeFactoriesTest {
    JsonNodeFactory schemaFactory = new JsonNodeFactory() {
        @Override
        public JsonNode wrap(Object node) {
            throw new IllegalArgumentException("schema");
        }
        @Override
        public JsonNode create(String rawJson) {
            throw new IllegalArgumentException("schema");
        }
    };
    JsonNodeFactory instanceFactory = new JsonNodeFactory() {
        @Override
        public JsonNode wrap(Object node) {
            throw new IllegalArgumentException("instance");
        }
        @Override
        public JsonNode create(String rawJson) {
            throw new IllegalArgumentException("instance");
        }
    };
    static class HolderFactory implements JsonNodeFactory {
        private JsonNodeFactory factory;

        void setFactory(JsonNodeFactory factory) {
            this.factory = factory;
        }

        @Override
        public JsonNode wrap(Object node) {
            return factory.wrap(node);
        }
        @Override
        public JsonNode create(String rawJson) {
            return factory.create(rawJson);
        }
    };
    JsonNode jsonNode = mock(JsonNode.class);
    Object providerNode = new ObjectMapper().readTree("{}");

    JsonNodeFactoriesTest() throws JsonProcessingException {}

    @Test
    void shouldUseSchemaFactoryInValidatorFactory() {
        ValidatorFactory factory = new ValidatorFactory().withJsonNodeFactories(schemaFactory, new JacksonNode.Factory());

        assertThatThrownBy(() -> factory.validate("{}", "{}")).hasMessage("schema");
        assertThatThrownBy(() -> factory.validate("{}", jsonNode)).hasMessage("schema");
        assertThatThrownBy(() -> factory.validate("{}", providerNode)).hasMessage("schema");
        assertThatThrownBy(() -> factory.validate(providerNode, "{}")).hasMessage("schema");
        assertThatThrownBy(() -> factory.validate(providerNode, jsonNode)).hasMessage("schema");
        assertThatThrownBy(() -> factory.validate(providerNode, providerNode)).hasMessage("schema");
    }

    @Test
    void shouldUseInstanceFactoryInValidatorFactory() {
        ValidatorFactory factory = new ValidatorFactory().withJsonNodeFactories(new JacksonNode.Factory(), instanceFactory);

        assertThatThrownBy(() -> factory.validate("{}", "{}")).hasMessage("instance");
        assertThatThrownBy(() -> factory.validate("{}", providerNode)).hasMessage("instance");
        assertThatThrownBy(() -> factory.validate(providerNode, "{}")).hasMessage("instance");
        assertThatThrownBy(() -> factory.validate(providerNode, providerNode)).hasMessage("instance");
        assertThatThrownBy(() -> factory.validate(jsonNode, "{}")).hasMessage("instance");
        assertThatThrownBy(() -> factory.validate(jsonNode, providerNode)).hasMessage("instance");
    }

    @Test
    void shouldUseSchemaFactoryInValidator() {
        Validator validator = new ValidatorFactory().withJsonNodeFactories(schemaFactory, new JacksonNode.Factory()).createValidator();
        URI uri = URI.create("urn:test");

        assertThatThrownBy(() -> validator.registerSchema("{}")).hasMessage("schema");
        assertThatThrownBy(() -> validator.registerSchema(providerNode)).hasMessage("schema");
        assertThatThrownBy(() -> validator.registerSchema(uri, "{}")).hasMessage("schema");
        assertThatThrownBy(() -> validator.registerSchema(uri, providerNode)).hasMessage("schema");
    }

    @Test
    void shouldUseInstanceFactoryInValidator() {
        Validator validator = new ValidatorFactory().withJsonNodeFactories(new JacksonNode.Factory(), instanceFactory).createValidator();
        URI uri = URI.create("urn:test");

        assertThatThrownBy(() -> validator.validate(uri, "{}")).hasMessage("instance");
        assertThatThrownBy(() -> validator.validate(uri, providerNode)).hasMessage("instance");
    }

    @Test
    void shouldUseSchemaFactoryWhenResolvingRootSchema() {
        SchemaResolver resolver = uri -> SchemaResolver.Result.fromString("{}");
        Validator validator = new ValidatorFactory().withJsonNodeFactories(schemaFactory, new JacksonNode.Factory())
                .withSchemaResolver(resolver)
                .createValidator();
        URI uri = URI.create("urn:test");

        assertThatThrownBy(() -> validator.validate(uri, "{}")).hasMessage("schema");
    }

    @Test
    void shouldUseSchemaFactoryWhenResolvingSchema() {
        SchemaResolver resolver = uri -> SchemaResolver.Result.fromString("{}");
        HolderFactory holderFactory = new HolderFactory();
        holderFactory.setFactory(new JacksonNode.Factory());
        Validator validator = new ValidatorFactory().withJsonNodeFactories(holderFactory, new JacksonNode.Factory())
                .withSchemaResolver(resolver)
                .createValidator();
        URI uri = URI.create("urn:test");
        JacksonNode jacksonNode = new JacksonNode.Factory().create("""
                {
                  "$ref": "urn:x"
                }""");
        validator.registerSchema(uri, jacksonNode);

        holderFactory.setFactory(schemaFactory);
        assertThatThrownBy(() -> validator.validate(uri, "{}")).hasMessage("schema");
    }

    @Test
    void shouldUseSchemaFactoryWhenResolvingMetaSchema() {
        SchemaResolver resolver = uri -> SchemaResolver.Result.fromString("{}");
        Validator validator = new ValidatorFactory().withJsonNodeFactories(schemaFactory, new JacksonNode.Factory())
                .withSchemaResolver(resolver)
                .createValidator();
        URI uri = URI.create("urn:test");
        JacksonNode jacksonNode = new JacksonNode.Factory().create("{}");

        Exception exception = catchException(() -> validator.registerSchema(uri, jacksonNode));
        assertThat(exception).isInstanceOf(MetaSchemaResolvingException.class);
        assertThat(exception.getCause()).hasMessage("schema");
    }
}
