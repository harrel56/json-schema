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
To enable spanish translations you can change the JVM default locale as follows:
```java
/* Ensure it's called before instantiating ValidatorFactory */
Locale.setDefault(Locale.of("es", "ES"));
```

You can also change locale explicitly for each `ValidatorFactory` object:
```java
MessageProvider messageProvider = MessageProvider.fromLocale(Locale.of("es", "ES"));
new ValidatorFactory().withMessageProvider(messageProvider);
```

If you require more customization you can pass your own instance of `ResourceBundle`.
This way you can change the location from which the bundles are loaded:
```java
ResourceBundle bundle = ResourceBundle.getBundle("your.package.bundles");
MessageProvider messageProvider = MessageProvider.fromResourceBundle(Locale.getDefault(), bundle);
new ValidatorFactory().withMessageProvider(messageProvider);
```

> Be aware that changing resource bundle location will cause the default english translations to not be loaded
> and you will be required to provide a translation for every keyword
> as for any missing translations a `MissingResourceException` will be thrown. 
{style="warning"}