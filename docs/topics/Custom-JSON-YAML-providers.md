# Custom JSON/YAML providers

Adding custom providers seems simple, but there are a lot of edge cases that need to be considered.
It is recommended to base your implementation on the officially provided providers (like [this one](https://github.com/harrel56/json-schema/blob/master/src/main/java/dev/harrel/jsonschema/providers/JacksonNode.java)).

## Running the test suite

> To ensure that a provider will produce valid results according to the JSON Schema specification, 
> it needs to pass the whole test suite.

Start by cloning the repository:
```shell
git clone https://github.com/harrel56/json-schema.git
```

Create a new directory for your provider test:
```shell
mkdir json-schema/src/integration/customJson
```

Create actual test implementation:
```java
// json-schema/src/integration/customJson/CustomJsonTest.java
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.ProviderTestBundle;

class CustomJsonTest extends ProviderTestBundle {
    @Override
    public JsonNodeFactory getJsonNodeFactory() {
        return new CustomJsonNode.Factory();
    }
}
```

Add the actual JSON library dependency to `build.gradle` to `jsonProviders` array (note that `id` must match the created directory name):
```groovy
def jsonProviders = [
        [id   : 'customJson', additionalVersions: [],
         group: 'com.custom.json', name: 'json-library', version: '1.0.0'],
        // ... other entries omitted for brevity
]
```

Run the tests:
```shell
cd json-schema \
./gradlew customJsonTest
```