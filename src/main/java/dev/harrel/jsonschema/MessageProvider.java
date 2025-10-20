package dev.harrel.jsonschema;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

public interface MessageProvider {
    String getMessage(String key, Object... args);

    static MessageProvider fromResourceBundle(ResourceBundle resourceBundle) {
        return new ResourceMessageProvider(resourceBundle);
    }
}

class ResourceMessageProvider implements MessageProvider {
    private final ResourceBundle resourceBundle;

    ResourceMessageProvider(ResourceBundle resourceBundle) {
        this.resourceBundle = Objects.requireNonNull(resourceBundle);
    }

    @Override
    public String getMessage(String key, Object... args) {
        String template = resourceBundle.getString(key);
        if (args.length == 0) {
            return template;
        }
        return MessageFormat.format(template, args);
    }
}
