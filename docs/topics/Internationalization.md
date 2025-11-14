# Internationalization

By default, validation messages are only available in english. 
However, all messages can be translated and customized using standard [ResourceBundle](https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html) mechanics.
[MessageFormat](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html) is used for formatting.

## Providing translations

In order to, for example, add spanish translations, you will need to create a 
`messages_es.properties` (or `messages_es_ES.properties` or any valid [language tag](https://developer.mozilla.org/en-US/docs/Glossary/BCP_47_language_tag)).
It needs to be placed in `dev.harrel.jsonschema` package and ideally contain translations of all standard keyword messages:
```properties
# java/resources/dev/harrel/jsonschema/messages_es.properties

type=...
const=...
...
```
Any missing translations will fall back to english ones.

By default, JVM locale is picked (`Locale.getDefault()`). 
To enable spanish translations you need to change the JVM default locale as follows:
```java
/* Ensure it's called before instantiating ValidatorFactory */
Locale.setDefault(Locale.of("es", "ES"));
```

You can also change locale for each `ValidatorFactory` object:
```java
ResourceBundle spanishBundle = ResourceBundle.getBundle("dev.harrel.jsonschema", Locale.of("es", "ES"));
new ValidatorFactory().withMessageProvider(MessageProvider.fromResourceBundle(spanishBundle));
```

> Changing location of resource bundle is also possible if required,
> but be aware that doing so will lose the default english translations
> and you will be required to provide a translation for every keyword
> as for any missing translation a `MissingResourceException` will be thrown.