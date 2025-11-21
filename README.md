# json-schema

[![build](https://github.com/harrel56/json-schema/actions/workflows/tests.yml/badge.svg)](https://github.com/harrel56/json-schema/actions/workflows/tests.yml)
[![maven](https://img.shields.io/maven-central/v/dev.harrel/json-schema?label=maven%20central&color=%2340ba12)](https://mvnrepository.com/artifact/dev.harrel/json-schema)
[![javadoc](https://javadoc.io/badge2/dev.harrel/json-schema/javadoc.svg)](https://javadoc.io/doc/dev.harrel/json-schema)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=harrel56_json-schema&metric=coverage)](https://sonarcloud.io/summary/new_code?id=harrel56_json-schema)
[![Size](https://jarhell.harrel.dev/api/v1/badges/total_size/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)
[![Bytecode](https://jarhell.harrel.dev/api/v1/badges/effective_bytecode/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)
[![Dependencies](https://jarhell.harrel.dev/api/v1/badges/dependencies/dev.harrel:json-schema)](https://jarhell.harrel.dev/packages/dev.harrel:json-schema)

Documentation: https://harrel56.github.io/json-schema

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for all recent [specification versions](https://harrel56.github.io/json-schema/dialects.html) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-dev.harrel.json-schema%2Fsupported_versions.json&color=blue)](https://bowtie.report/#/implementations/java-json-schema):
  - Draft 2020-12 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2020-12.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2020-12),
  - Draft 2019-09 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2019-09.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2019-09),
  - Draft 07 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft7.json&color=brightgreen)](https://bowtie.report/#/dialects/draft7),
  - Draft 06 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft6.json&color=brightgreen)](https://bowtie.report/#/dialects/draft6),
  - Draft 04 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft4.json&color=brightgreen)](https://bowtie.report/#/dialects/draft4),
- support for [custom keywords](https://harrel56.github.io/json-schema/evaluators.html),
- support for [internationalization](https://harrel56.github.io/json-schema/internationalization.html) of error messages,
- support for [annotation collection](https://harrel56.github.io/json-schema/annotations.html),
- support for [format validation](https://harrel56.github.io/json-schema/format-validation.html) (for a price of one additional dependency 😉),
- compatible with most of the JSON/YAML libraries ([supported libraries](https://harrel56.github.io/json-schema/json-yaml-providers.html) - Jackson 3.x available now too!),
- and no additional dependencies on top of that.

Check how it compares with other implementations:
- [Bowtie](https://bowtie.report) - specification compliance (only mandatory behavior),
- [Creek's benchmark](https://www.creekservice.org/json-schema-validation-comparison/performance) - benchmark for JVM based implementations.

## Demo
You can check out how it works [here](https://harrel.dev/json-schema).

## Installation
Please note that you will also need to include at least one of the supported JSON provider libraries (see [JSON provider setup](https://harrel56.github.io/json-schema/json-yaml-providers.html)).

### Maven
```xml
<dependency>
    <groupId>dev.harrel</groupId>
    <artifactId>json-schema</artifactId>
    <version>1.8.2</version>
</dependency>
```

### Gradle
```groovy
implementation 'dev.harrel:json-schema:1.8.2'
```

## Usage
To validate JSON against a schema, you just need to invoke:
```java
String schema = """
        {
          "type": "boolean"
        }""";
String instance = "true";
boolean valid = new ValidatorFactory().validate(schema, instance).isValid();
```

## Documentation
For more details please visit the [documentation](https://harrel56.github.io/json-schema).
