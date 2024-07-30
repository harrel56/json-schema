package dev.harrel.jsonschema;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.harrel.jsonschema.SimpleType.*;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

abstract class EvaluatorFactoryTest implements ProviderTest {
    private static final Map<SimpleType, String> TYPE_MAP = Map.of(
            NULL, "null",
            BOOLEAN, "true",
            STRING, "\"string\"",
            INTEGER, "1",
            NUMBER, "1.1",
            ARRAY, "[]",
            OBJECT, "{}"
    );
    private final SchemaParsingContext ctx;

    EvaluatorFactoryTest(SpecificationVersion version) {
        MetaSchemaData metaData = new MetaSchemaData(Dialects.OFFICIAL_DIALECTS.get(URI.create(version.getId())));
        this.ctx = new SchemaParsingContext(metaData, new SchemaRegistry(), URI.create("urn:CoreEvaluatorFactoryTest"), emptyMap());
    }

    void testSupportedKeyword(String keyword, Set<SimpleType> supportedTypes) {
        EvaluatorFactory evaluatorFactory = ctx.getMetaValidationData().dialect.getEvaluatorFactory();
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = evaluatorFactory.create(ctx, keyword, wrappedNode.asObject().get(keyword));
            if (supportedTypes.contains(entry.getKey())) {
                assertThat(evaluator)
                        .withFailMessage("Expected type [%s] to pass", entry.getKey())
                        .isPresent();
            } else {
                assertThat(evaluator)
                        .withFailMessage("Expected type [%s] to fail", entry.getKey())
                        .isEmpty();
            }
        }
    }

    void testUnsupportedKeyword(String keyword) {
        EvaluatorFactory evaluatorFactory = ctx.getMetaValidationData().dialect.getEvaluatorFactory();
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = evaluatorFactory.create(ctx, keyword, wrappedNode.asObject().get(keyword));
            if (entry.getKey() == STRING) {
                assertThat(evaluator).containsInstanceOf(AbstractEvaluatorFactory.AnnotationEvaluator.class);
            } else {
                assertThat(evaluator).isEmpty();
            }
        }
    }

    void testIgnoredKeyword(String keyword) {
        EvaluatorFactory evaluatorFactory = ctx.getMetaValidationData().dialect.getEvaluatorFactory();
        for (var entry : TYPE_MAP.entrySet()) {
            JsonNode wrappedNode = getJsonNodeFactory().create("{\"%s\": %s}".formatted(keyword, entry.getValue()));
            Optional<Evaluator> evaluator = evaluatorFactory.create(ctx, keyword, wrappedNode.asObject().get(keyword));
            assertThat(evaluator).isEmpty();
        }
    }
}