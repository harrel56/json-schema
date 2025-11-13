package dev.harrel.jsonschema;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceMessageProviderTest {
    private static Locale defaultLocale;

    @BeforeAll
    static void beforeAll() {
        defaultLocale = Locale.getDefault();
    }

    @AfterAll
    static void afterAll() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void shouldCascadeReadProperties() {
        Locale.setDefault(Locale.of("es", "ES", "variant1"));
        ResourceMessageProvider provider = ResourceMessageProvider.createDefault();

        assertThat(provider.getMessage("type")).isEqualTo("type (es-ES-variant1)");
        assertThat(provider.getMessage("const")).isEqualTo("const (es-ES)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (es)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    @Test
    void shouldCascadeReadPropertiesWithScript() {
        Locale.setDefault(Locale.forLanguageTag("es-Latn-AR"));
        ResourceMessageProvider provider = ResourceMessageProvider.createDefault();

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
        ResourceMessageProvider provider = ResourceMessageProvider.createDefault();

        assertThat(provider.getMessage("type")).isEqualTo("type (fr-FR)");
        assertThat(provider.getMessage("enum")).isEqualTo("enum (fr-FR)");
        assertThat(provider.getMessage("const")).isEqualTo("const (fr)");
        assertThat(provider.getMessage("not")).isEqualTo("Value matches against given schema but it must not");
    }

    class MapBundle  extends ResourceBundle {
        private final Map<String, String> messages;

        MapBundle(Map<String, String> messages) {this.messages = messages;}

        @Override
        protected Object handleGetObject(String key) {
            return messages.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    }
}