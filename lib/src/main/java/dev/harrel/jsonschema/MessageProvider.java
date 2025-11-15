package dev.harrel.jsonschema;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * {@code MessageProvider} interface provides a strategy for supplying error/validation messages.
 * For the majority of cases, the default implementation which comes with every {@link Validator} and {@link ValidatorFactory} is sufficient.
 * If you only want to:
 * <ul>
 *     <li>provide a message for a custom keyword,</li>
 *     <li>add translations for existing keywords,</li>
 *     <li>override an existing error message</li>
 * </ul>
 * you can put a translations file {@code messages_{lang}_{country}.properties} in a {@code dev.harrel.jsonschema} package,
 * and it will be picked up automatically.
 * If you want to use different location, you can achieve it by calling {@link MessageProvider#fromResourceBundle(ResourceBundle)}
 * with your customized {@link ResourceBundle}
 *
 * @implNote Please note that a default implementation will throw {@link java.util.MissingResourceException}
 * when there is no message for given key.
 */
public interface MessageProvider {
    /**
     * Returns a final message which will be presented to the user.
     * It will be called only for failures registered via {@link Evaluator.Result#formattedFailure(String, Object...)}.
     * @param key key of the message template
     * @param args additional arguments for the message template
     * @return final internationalized message
     */
    String getMessage(String key, Object... args);

    /**
     * Returns a {@code MessageProvider} implementation based on a {@link ResourceBundle}.
     * @param resourceBundle a resource bundle to use
     * @return {@code MessageProvider} implementation based on a {@link ResourceBundle}
     */
    static MessageProvider fromResourceBundle(ResourceBundle resourceBundle) {
        return new ResourceMessageProvider(resourceBundle);
    }
}

final class ResourceMessageProvider implements MessageProvider {
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
