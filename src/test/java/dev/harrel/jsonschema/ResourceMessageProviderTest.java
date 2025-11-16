package dev.harrel.jsonschema;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceMessageProviderTest {
    private static Locale defaultLocale;

    @BeforeEach
    void setUp() {
        defaultLocale = Locale.getDefault();
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void shouldCascadeReadProperties() {
        Locale.setDefault(Locale.of("es", "ES", "variant1"));
        MessageProvider provider = MessageProvider.fromResourceBundle(createDefaultBundle());

        assertThat(provider.getMessage("type")).isEqualTo("type (es-ES-variant1)");
        assertThat(provider.getMessage("const")).isEqualTo("const (es-ES)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (es)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    @Test
    void shouldCascadeReadPropertiesWithExplicitLocale() {
        Locale locale = Locale.of("es", "ES", "variant1");
        String bundleName = createDefaultBundle().getBaseBundleName();
        MessageProvider provider = MessageProvider.fromResourceBundle(ResourceBundle.getBundle(bundleName, locale));

        assertThat(provider.getMessage("type")).isEqualTo("type (es-ES-variant1)");
        assertThat(provider.getMessage("const")).isEqualTo("const (es-ES)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (es)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    @Test
    void shouldCascadeReadPropertiesWithScript() {
        Locale.setDefault(Locale.forLanguageTag("es-Latn-AR"));
        MessageProvider provider = MessageProvider.fromResourceBundle(createDefaultBundle());

        assertThat(provider.getMessage("type")).isEqualTo("type (es-AR)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (es-AR)");
        assertThat(provider.getMessage("const")).isEqualTo("const (es)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    // Gradle has some troubles compiling class resource bundles (messages_fr.java)
    // might need to run gradle clean
    @Test
    void shouldCascadeReadJavaClassBundles() {
        Locale.setDefault(Locale.of("fr", "FR"));
        MessageProvider provider = MessageProvider.fromResourceBundle(createDefaultBundle());

        assertThat(provider.getMessage("type")).isEqualTo("type (fr-FR)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (fr-FR)");
        assertThat(provider.getMessage("const")).isEqualTo("const (fr)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    @Test
    void shouldFailForMissingKey() {
        MessageProvider provider = MessageProvider.fromResourceBundle(createDefaultBundle());
        assertThatThrownBy(() -> provider.getMessage("missingKey", 1, 2))
                .isInstanceOf(MissingResourceException.class)
                .hasMessage("Can't find resource for bundle java.util.PropertyResourceBundle, key missingKey");
    }

    @Test
    void shouldSupportMapBasedBundle() {
        Locale.setDefault(Locale.of("es", "ES", "variant1"));
        MessageProvider provider = MessageProvider.fromResourceBundle(new MapBundle(Map.of("type", "type (map)")));

        assertThat(provider.getMessage("type")).isEqualTo("type (map)");
        assertThat(provider.getMessage("const")).isEqualTo("const (es-ES)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (es)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    @Test
    void shouldNotLosePrecisionOnNumberArguments() {
        MessageProvider provider = MessageProvider.fromResourceBundle(new MapBundle(Map.of("test", "{0}")));

        assertThat(provider.getMessage("test", 0)).isEqualTo("0");
        assertThat(provider.getMessage("test", -1)).isEqualTo("-1");
        assertThat(provider.getMessage("test", 1.0)).isEqualTo("1.0");
        assertThat(provider.getMessage("test", 1.0000000001)).isEqualTo("1.0000000001");
        assertThat(provider.getMessage("test", 0.9999999999)).isEqualTo("0.9999999999");
        BigInteger bigInt = new BigInteger("9999999999999999999999999999999999999999999999999999999999999999");
        assertThat(provider.getMessage("test", bigInt)).isEqualTo("9999999999999999999999999999999999999999999999999999999999999999");
        BigDecimal bigDec = new BigDecimal("3.14159265358979323846264338327950288419716939937510582097494459230");
        assertThat(provider.getMessage("test", bigDec)).isEqualTo("3.14159265358979323846264338327950288419716939937510582097494459230");
    }

    @Test
    void explicitFormatShouldOverrideNumberFormatting() {
        MessageProvider provider = MessageProvider.fromResourceBundle(new MapBundle(Map.of(
                "number", "{0,number}",
                "integer", "{0,number,integer}",
                "percent", "{0,number,percent}",
                "currency", "{0,number,currency}",
                "places", "{0,number,#.#####}",
                "choice", "{0,choice,0#nope|1#yes|2#more yes}"
        )));

        assertThat(provider.getMessage("number", 0.00000000000001)).isEqualTo("0");
        assertThat(provider.getMessage("integer", 0.1)).isEqualTo("0");
        assertThat(provider.getMessage("percent", 0.01)).isEqualTo("1%");
        assertThat(provider.getMessage("currency", 0.001)).isEqualTo("$0.00");
        assertThat(provider.getMessage("places", 1.123456)).isEqualTo("1.12346");
        assertThat(provider.getMessage("choice", 0)).isEqualTo("nope");
        assertThat(provider.getMessage("choice", 1)).isEqualTo("yes");
        assertThat(provider.getMessage("choice", 10)).isEqualTo("more yes");
    }

    private static ResourceBundle createDefaultBundle() {
        return ResourceBundle.getBundle("dev.harrel.jsonschema.messages");
    }

    static class MapBundle extends ResourceBundle {
        private final Map<String, String> messages;

        MapBundle(Map<String, String> messages) {
            this.messages = messages;
            this.setParent(createDefaultBundle());
        }

        @Override
        protected Object handleGetObject(String key) {
            return messages.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(messages.keySet());
        }
    }
}