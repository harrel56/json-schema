# About

`dev.harrel:json-schema` is a Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for all recent [specification versions](Dialects.md) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-dev.harrel.json-schema%2Fsupported_versions.json&color=blue)](https://bowtie.report/#/implementations/java-json-schema):
    - [Draft 2020-12](https://www.learnjsonschema.com/2020-12/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2020-12.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2020-12),
    - [Draft 2019-09](https://www.learnjsonschema.com/2019-09/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2019-09.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2019-09),
    - [Draft 07](https://www.learnjsonschema.com/draft7/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft7.json&color=brightgreen)](https://bowtie.report/#/dialects/draft7),
    - [Draft 06](https://www.learnjsonschema.com/draft6/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft6.json&color=brightgreen)](https://bowtie.report/#/dialects/draft6),
    - [Draft 04](https://www.learnjsonschema.com/draft4/) [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft4.json&color=brightgreen)](https://bowtie.report/#/dialects/draft4),
- support for [custom keywords](Custom-keywords.md),
- support for [annotation collection](Annotations.md),
- support for [format validation](Format-validation.md) (for a price of one additional dependency 😉),
- compatible with most of the JSON/YAML libraries ([supported libraries](JSON-YAML-providers.md)),
- and no additional dependencies on top of that.