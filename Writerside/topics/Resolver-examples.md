# Resolver examples

## HTTP resolver

```java
import dev.harrel.jsonschema.SchemaResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpResolver implements SchemaResolver {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Result resolve(String uriString) {
        URI uri = URI.create(uriString);
        if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
            return Result.empty();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Result.fromString(response.body());
            }
            return Result.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read schema via http(s): " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }
}

```

## Classpath resolver

```java
import dev.harrel.jsonschema.SchemaResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ClasspathResolver implements SchemaResolver {
    @Override
    public Result resolve(String uriString) {
        URI uri = URI.create(uriString);
        if (!uri.getScheme().equals("classpath")) {
            return Result.empty();
        }
        try (InputStream is = ClasspathResolver.class.getResourceAsStream(uri.getSchemeSpecificPart())) {
            if (is == null) {
                return Result.empty();
            }
            String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Result.fromString(schema);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read classpath resource: " + uri, e);
        }
    }
}
```
