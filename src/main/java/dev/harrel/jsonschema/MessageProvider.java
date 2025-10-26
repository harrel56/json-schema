package dev.harrel.jsonschema;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
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
        MessageFormat messageFormat = new MessageFormat(template);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Number) {
                messageFormat.setFormat(i, new Format() {
                    @Override
                    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
                        System.out.println(obj.getClass());
                        return toAppendTo.append(obj.toString());
                    }

                    @Override
                    public Object parseObject(String source, @NotNull ParsePosition pos) {
                        return null;
                    }
                });
            }
        }
        return messageFormat.format(args);
    }
}
