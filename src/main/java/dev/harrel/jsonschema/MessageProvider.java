package dev.harrel.jsonschema;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * todo
 */
public interface MessageProvider {
    String getMessage(String key, Object... args);

    static MessageProvider fromResourceBundle(ResourceBundle resourceBundle) {
        return new ResourceMessageProvider(resourceBundle);
    }
}

final class ResourceMessageProvider implements MessageProvider {
    private final ResourceBundle resourceBundle;

    ResourceMessageProvider(ResourceBundle resourceBundle) {
        this.resourceBundle = Objects.requireNonNull(resourceBundle);
    }

    static ResourceMessageProvider createDefault() {
        return new ResourceMessageProvider(ResourceBundle.getBundle("dev.harrel.jsonschema.messages"));
    }

    @Override
    public String getMessage(String key, Object... args) {
        String template = resourceBundle.getString(key);
        if (args.length == 0) {
            return template;
        }
        MessageFormat messageFormat = new MessageFormat(template);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Number) {
                messageFormat.setFormatByArgumentIndex(i, new StringFormat());
            }
        }
        return messageFormat.format(args);
    }

    private static final class StringFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            throw new UnsupportedOperationException();
        }
    }
}
