package dev.harrel.jsonschema;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadSafetyTest {
    private static ExecutorService threadPool;

    @BeforeAll
    static void beforeAll() {
        threadPool = Executors.newFixedThreadPool(100);
    }

    @AfterAll
    static void afterAll() {
        threadPool.shutdownNow();
    }

    @AfterEach
    void tearDown() {
//        threadPool.shutdownNow();
    }

    @RepeatedTest(100)
    void threadSafetyRegistrationTest() throws InterruptedException, ExecutionException {
        String schema = """
                {
                  "$schema": "urn:meta"
                }""";
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> SchemaResolver.Result.fromString("true"))
                .createValidator();

            List<Callable<URI>> callables = IntStream.range(0, 1000)
                    .mapToObj(i -> URI.create("urn:" + i))
                    .map(uri -> (Callable<URI>) () -> validator.registerSchema(uri, schema))
                    .toList();
        List<Future<URI>> futures = threadPool.invokeAll(callables);
        for (Future<URI> future : futures) {
            future.get();
        }
//        IntStream.range(0, 1000).parallel().forEach(i -> validator.registerSchema(schema));
            URI uri = validator.registerSchema(schema);
            assertThat(validator.validate(uri, "true").isValid()).isTrue();
    }

    @RepeatedTest(5)
    void threadSafetyRegistrationTest2() {
        String schema = """
                {
                  "$ref": "urn:any"
                }""";
        Validator validator = new ValidatorFactory()
                .withSchemaResolver(uri -> {
                    try {
                        System.out.println(uri);
                        Thread.sleep(400);
                        return SchemaResolver.Result.fromString("true");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .createValidator();

        URI uri = validator.registerSchema(schema);
        IntStream.range(0, 100).parallel().forEach(i -> validator.validate(uri, "{}"));
        assertThat(validator.validate(uri, "true").isValid()).isTrue();
    }
}
