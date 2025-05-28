# Format validation

By default, the ` format ` keyword performs no validation (only collects annotations as mandated by the JSON Schema specification).
Format validation requires additional dependency:

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