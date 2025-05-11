# About

Java library implementing [JSON schema specification](https://json-schema.org/specification.html):
- compatible with Java 8,
- support for all recent [specification versions](#dialects) [![Supported spec](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie-json-schema.github.io%2Fbowtie%2Fbadges%2Fjava-dev.harrel.json-schema%2Fsupported_versions.json&color=blue)](https://bowtie.report/#/implementations/java-json-schema):
    - Draft 2020-12 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2020-12.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2020-12),
    - Draft 2019-09 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft2019-09.json&color=brightgreen)](https://bowtie.report/#/dialects/draft2019-09),
    - Draft 07 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft7.json&color=brightgreen)](https://bowtie.report/#/dialects/draft7),
    - Draft 06 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft6.json&color=brightgreen)](https://bowtie.report/#/dialects/draft6),
    - Draft 04 [![Compliance](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-dev.harrel.json-schema%2Fcompliance%2Fdraft4.json&color=brightgreen)](https://bowtie.report/#/dialects/draft4),
- support for [custom keywords](#adding-custom-keywords),
- support for annotation collection,
- support for [format validation](#format-validation) (for a price of one additional dependency 😉),
- compatible with most of the JSON/YAML libraries ([supported libraries](#jsonyaml-providers)),
- and no additional dependencies on top of that.