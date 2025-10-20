package dev.harrel.jsonschema;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ResourceMessageProviderTest {
    @Test
    void name() {
//        var rb = ResourceBundle.getBundle("dev.harrel.jsonschema.messages", Locale.of("pl"));
//        System.out.println(rb.getString("validator.type"));
//        System.out.println(rb.getString("greet"));

        MessageProvider provider = MessageProvider.fromResourceBundle(ResourceBundle.getBundle("dev.harrel.jsonschema.messages", Locale.of("pl")));
        Object[] args = new Object[]{0};
        provider.getMessage("validator.type", args);
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