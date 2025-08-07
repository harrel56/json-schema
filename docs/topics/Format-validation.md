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

To enable format validation, attach [FormatEvaluatorFactory](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/FormatEvaluatorFactory.html)
to your `ValidatorFactory` instance:

```java
new ValidatorFactory().withEvaluatorFactory(new FormatEvaluatorFactory());
```

> You can have multiple factories using:
> ```java
> new ValidatorFactory()
>     .withEvaluatorFactory(EvaluatorFactory.compose(customFactory, new FormatEvaluatorFactory()));
> ```

## Supported formats

> Note that provided format validation is not 100% specification compliant.
> Instead, it focuses on being more "Java environment oriented."
> So, for example, when a `value` is validated as being in `uri-reference` format,
> it is guaranteed that `URI.create(value)` call will succeed.

- **date**, **date-time**, **time** - uses `java.time.format.DateTimeFormatter` with standard ISO formatters,
- **duration** - uses regex validation as it may be a combination of `java.time.Duration` and `java.time.Period`,
- **email**, **idn-email** - uses `com.sanctionco.jmail.JMail`,
- **hostname** - uses regex validation,
- **idn-hostname** - not supported - performs same validation as `hostname`,
- **ipv4**, **ipv6** - uses `com.sanctionco.jmail.net.InternetProtocolAddress`,
- **uri**, **uri-reference**, **iri**, **iri-reference** - uses `java.net.URI`,
- **uuid** - uses `java.util.UUID`,
- **uri-template** - lenient checking of unclosed braces (compatible with Spring's implementation),
- **json-pointer**, **relative-json-pointer** - uses manual validation,
- **regex** - uses `java.util.regex.Pattern`.

## Vocabulary compliance

Specification defines a way to turn on and off format validation using [vocabularies](https://json-schema.org/draft/2020-12/json-schema-core#name-meta-schemas-and-vocabulari),
but due to it being cumbersome to use, it is not the default way to achieve this.
This means, if you attach `FormatEvaluatorFactory` to your `ValidatorFactory`,
it will validate formats regardless of the vocabularies' state.

If you still want to use vocabularies, you can use a special `FormatEvaluatorFactory` constructor,
which accepts a set of strings (vocabulary names):

```java
new FormatEvaluatorFactory(Set.of(Vocabulary.Draft2020.FORMAT_ASSERTION))
```

This will only enable format validation when `Vocabulary.Draft2020.FORMAT_ASSERTION` is enabled.

> It is possible to use any vocabulary or use multiple vocabularies.
> Using multiple vocabularies will enable validation whenever **ANY** of the provided vocabularies is enabled.

## Adding custom formats

Adding custom formats is generally the same as adding custom keywords.
See [Custom keywords](Evaluators.md) for more information.
You can add new formats on top of the default ones by using `EvaluatorFactory.compose` or
by composition and delegation to `FormatEvaluatorFactory`.