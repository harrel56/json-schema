package dev.harrel.jsonschema;

import dev.harrel.jsonschema.providers.JacksonNode;
import dev.harrel.jsonschema.util.TestUtil;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class ThreadSafetyTest {

    abstract ExecutorService getExecutorService();

    static class PlatformThreadsTest extends ThreadSafetyTest {
        private static ExecutorService threadPool;

        @BeforeAll
        static void beforeAll() {
            threadPool = Executors.newFixedThreadPool(100);
        }

        @AfterAll
        static void afterAll() {
            threadPool.shutdownNow();
        }

        @Override
        ExecutorService getExecutorService() {
            return threadPool;
        }
    }

    static class VirtualThreadsTest extends ThreadSafetyTest {
        private static ExecutorService threadPool;

        @BeforeAll
        static void beforeAll() {
            threadPool = Executors.newVirtualThreadPerTaskExecutor();
        }

        @AfterAll
        static void afterAll() {
            threadPool.shutdownNow();
        }

        @Override
        ExecutorService getExecutorService() {
            return threadPool;
        }
    }

    @Test
    void concurrentDirectRegistrations() throws InterruptedException, ExecutionException {
        Validator validator = new ValidatorFactory().createValidator();
        List<URI> uris = IntStream.range(0, 1000)
                .mapToObj(i -> URI.create("urn:" + i))
                .toList();
        List<Callable<URI>> callables = uris.stream()
                .map(uri -> (Callable<URI>) () -> validator.registerSchema(uri, "{}"))
                .toList();
        completeAll(callables);

        for (URI uri : uris) {
            Validator.Result result = validator.validate(uri, "{}");
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    void concurrentRefResolutions() throws InterruptedException, ExecutionException {
        String schema = """
                {
                  "$ref": "urn:ref"
                }""";
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(new LatchedSchemaResolver(100))
                .createValidator();
        URI uri = validator.registerSchema(schema);
        List<Callable<Object>> callables = IntStream.range(0, 100)
                .mapToObj(i -> (Callable<Object>) () -> validator.validate(uri, "{}"))
                .toList();
        completeAll(callables);

        Validator.Result result = validator.validate(uri, "{}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void concurrentRootResolutions() throws InterruptedException, ExecutionException {
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(new LatchedSchemaResolver(100))
                .createValidator();
        List<URI> uris = IntStream.range(0, 100)
                .mapToObj(i -> URI.create("urn:" + i))
                .toList();
        List<Callable<Object>> callables = uris.stream()
                .map(uri -> (Callable<Object>) () -> validator.validate(uri, "{}"))
                .toList();
        completeAll(callables);

        for (URI uri : uris) {
            Validator.Result result = validator.validate(uri, "{}");
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    void concurrentMetaSchemaResolutions() throws InterruptedException, ExecutionException {
        String schema = """
                {
                  "$schema": "urn:ref"
                }""";
        Validator validator = new ValidatorFactory()
                .withDialect(new Dialects.Draft2020Dialect() {
                    @Override
                    public String getMetaSchema() {
                        return null;
                    }
                })
                .withSchemaResolver(new LatchedSchemaResolver(1))
                .createValidator();
        List<URI> uris = IntStream.range(0, 100)
                .mapToObj(i -> URI.create("urn:" + i))
                .toList();
        List<Callable<URI>> callables = uris.stream()
                .map(uri -> (Callable<URI>) () -> validator.registerSchema(uri, schema))
                .toList();
        completeAll(callables);

        for (URI uri : uris) {
            Validator.Result result = validator.validate(uri, "{}");
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    void concurrentRegistersWithValidates() throws InterruptedException, ExecutionException {
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(new LatchedSchemaResolver(1))
                .createValidator();
        URI validationUri = validator.registerSchema("{}");
        List<Callable<Object>> writes = IntStream.range(0, 20)
                .mapToObj(i -> URI.create("urn:" + i))
                .map(uri -> (Callable<Object>) () -> validator.validate(uri, "{}"))
                .toList();
        List<Callable<Object>> reads = IntStream.range(0, 80)
                .mapToObj(i -> (Callable<Object>) () -> validator.validate(validationUri, "{}"))
                .toList();
        List<Callable<Object>> callables = new ArrayList<>(writes);
        callables.addAll(reads);
        Collections.shuffle(callables);
        completeAll(callables);
    }

    private <T> void completeAll(List<Callable<T>> callables) throws ExecutionException, InterruptedException {
        List<Future<T>> futures = getExecutorService().invokeAll(callables);
        for (Future<T> future : futures) {
            future.get();
        }
    }

    private static class LatchedSchemaResolver implements SchemaResolver {
        private final CountDownLatch latch;
        private final JsonNode schemaNode;

        LatchedSchemaResolver(int latchSize) {
            this.latch = new CountDownLatch(latchSize);
            String schemaString = TestUtil.readResource("/schema.json");
            this.schemaNode = new JacksonNode.Factory().create(schemaString);
        }

        @Override
        public Result resolve(String uri) {
            if (Dialects.OFFICIAL_DIALECTS.containsKey(URI.create(uri))) {
                return Result.empty();
            }

            try {
                latch.countDown();
                if (!latch.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Latch timeout reached");
                }
                return Result.fromJsonNode(schemaNode);
            } catch (InterruptedException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
