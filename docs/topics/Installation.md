# Installation

This library is not tied to any JSON/YAML implementation, and you will need to include at least one of the [supported JSON/YAML libraries](JSON-YAML-providers.md).

> The default JSON/YAML implementation is `com.fasterxml.jackson.core:jackson-databind`.
> Ensure it is on your classpath if it was not configured to use any other implementation.

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>dev.harrel</groupId>
    <artifactId>json-schema</artifactId>
    <version>%latest_version%</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'dev.harrel:json-schema:%latest_version%'
```

</tab>
</tabs>

## Format validation (optional)

If you want to use [format validation](Format-validation.md), you will need to include one additional dependency:

<tabs group='build-tool'>
<tab title="Maven" group-key='maven'>

```xml
<dependency>
    <groupId>com.sanctionco.jmail</groupId>
    <artifactId>jmail</artifactId>
    <version>%jmail_version%</version>
</dependency>
```

</tab>
<tab title="Gradle" group-key='gradle'>

```groovy
implementation 'com.sanctionco.jmail:jmail:%jmail_version%'
```

</tab>
</tabs>
