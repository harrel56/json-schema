package dev.harrel.jsonschema;

import com.sanctionco.jmail.JMail;
import com.sanctionco.jmail.net.InternetProtocolAddress;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * {@code EvaluatorFactory} implementation that provides format validation capabilities.
 * It should not be used as a standalone factory.
 * It is intended to be used as a supplementary factory to a full dialect-compatible factory (like {@link Draft2020EvaluatorFactory}).
 * <pre>
 *     new ValidatorFactory().withEvaluatorFactory(new FormatEvaluatorFactory());
 * </pre>
 * May also be used in conjunction with custom factories:
 * <pre>
 *     new ValidatorFactory().withEvaluatorFactory(EvaluatorFactory.compose(customFactory, new FormatEvaluatorFactory()));
 * </pre>
 * <p>
 * It may not be fully compatible with JSON Schema specification. It is mostly based on tools already present in JDK itself.
 * Supported formats:
 * <ul>
 *     <li>
 *         <strong>date, date-time, time</strong> - uses {@link DateTimeFormatter} with standard ISO formatters,
 *     </li>
 *     <li>
 *          <strong>duration</strong> - regex based validation as it may be combination of {@link java.time.Duration} and {@link java.time.Period},
 *     </li>
 *     <li>
 *          <strong>email, idn-email</strong> - uses {@link JMail#isValid(String)},
 *     </li>
 *     <li>
 *          <strong>hostname</strong> - regex based validation,
 *     </li>
 *     <li>
 *          <strong>idn-hostname</strong> - not supported - performs same validation as <strong>hostname</strong>,
 *     </li>
 *     <li>
 *          <strong>ipv4, ipv6</strong> - uses {@link InternetProtocolAddress},
 *     </li>
 *     <li>
 *          <strong>uri, uri-reference, iri, iri-reference</strong> - uses {@link URI},
 *     </li>
 *     <li>
 *          <strong>uuid</strong> - uses {@link UUID},
 *     </li>
 *     <li>
 *          <strong>uri-template</strong> - lenient checking of unclosed braces (should be compatible with Spring's implementation),
 *     </li>
 *     <li>
 *          <strong>json-pointer, relative-json-pointer</strong> - manual validation,
 *     </li>
 *     <li>
 *          <strong>regex</strong> - uses {@link Pattern}.
 *     </li>
 * </ul>
 *
 * @implNote Default constructor provides instance without <i>vocabulary</i> support. This means the validation will
 * always occur regardless of currently active vocabularies (determined based on meta-schema).
 * If more specification compliant instance is needed, please explicitly provide vocabularies to the constructor:
 * <pre>
 *     new FormatEvaluatorFactory(Set.of(Vocabulary.Draft2020.FORMAT_ASSERTION, Vocabulary.Draft2019.FORMAT));
 * </pre>
 * Then the validation will only be run if at least one of provided vocabularies is active.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class FormatEvaluatorFactory implements EvaluatorFactory {
    private static final class FormatEvaluator implements Evaluator {
        private final Set<String> vocabularies;
        private final UnaryOperator<String> operator;

        private FormatEvaluator(Set<String> vocabularies, UnaryOperator<String> operator) {
            this.vocabularies = vocabularies;
            this.operator = operator;
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            if (!node.isString()) {
                return Result.success();
            }
            String value = node.asString();
            String err = operator.apply(value);
            return err == null ? Result.success(value) : Result.failure(err);
        }

        @Override
        public Set<String> getVocabularies() {
            return vocabularies;
        }
    }

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "P(?:\\d+W|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*",
            Pattern.CASE_INSENSITIVE
    );

    private final Set<String> vocabularies;

    /**
     * Creates a default instance without vocabularies support.
     */
    public FormatEvaluatorFactory() {
        this.vocabularies = emptySet();
    }

    /**
     * Creates a customized instance with vocabularies support.
     * Validation will only be run when at least one of provided vocabularies is active during validation process.
     */
    public FormatEvaluatorFactory(Set<String> vocabularies) {
        this.vocabularies = unmodifiableSet(new HashSet<>(vocabularies));
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext parsingCtx, String fieldName, JsonNode fieldNode) {
        if (!"format".equals(fieldName) || !fieldNode.isString()) {
            return Optional.empty();
        }
        return Optional.ofNullable(getOperator(fieldNode.asString()))
                .map(op -> new FormatEvaluator(vocabularies, op));
    }

    private UnaryOperator<String> getOperator(String format) {
        switch (format) {
            case "date":
                return tryOf(DateTimeFormatter.ISO_DATE::parse);
            case "date-time":
                return tryOf(DateTimeFormatter.ISO_DATE_TIME::parse);
            case "time":
                return tryOf(DateTimeFormatter.ISO_TIME::parse);
            case "duration":
                return v -> DURATION_PATTERN.matcher(v).matches() ? null : String.format("\"%s\" is not a valid duration string", v);
            case "email":
            case "idn-email":
                return v -> JMail.isValid(v) ? null : String.format("\"%s\" is not a valid email address", v);
            case "hostname":
            case "idn-hostname":
                return v -> HOSTNAME_PATTERN.matcher(v).matches() ? null : String.format("\"%s\" is not a valid hostname", v);
            case "ipv4":
                return v -> InternetProtocolAddress.validateIpv4(v).isPresent() ? null : String.format("\"%s\" is not a valid IPv4 address", v);
            case "ipv6":
                return v -> InternetProtocolAddress.validateIpv6(v).isPresent() ? null : String.format("\"%s\" is not a valid IPv6 address", v);
            case "uri":
            case "iri":
                return FormatEvaluatorFactory::uriOperator;
            case "uri-reference":
            case "iri-reference":
                return tryOf(URI::create);
            case "uuid":
                return FormatEvaluatorFactory::uuidOperator;
            case "uri-template":
                return FormatEvaluatorFactory::uriTemplateOperator;
            case "json-pointer":
                return v -> validateJsonPointer(v) ? null : String.format("\"%s\" is not a valid json-pointer", v);
            case "relative-json-pointer":
                return FormatEvaluatorFactory::rjpOperator;
            case "regex":
                return tryOf(Pattern::compile);
            default:
                return null;
        }
    }

    private static UnaryOperator<String> tryOf(Consumer<String> op) {
        return v -> {
            try {
                op.accept(v);
                return null;
            } catch (Exception e) {
                return e.getMessage();
            }
        };
    }

    private static boolean validateJsonPointer(String pointer) {
        if (pointer.isEmpty()) {
            return true;
        }
        if (!pointer.startsWith("/")) {
            return false;
        }
        String decoded = pointer.replace("~0", "").replace("~1", "");
        return !decoded.contains("~");
    }

    private static String uriOperator(String value) {
        try {
            URI uri = URI.create(value);
            return uri.isAbsolute() ? null : String.format("\"%s\" is a relative URI", uri);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String uuidOperator(String value) {
        try {
            UUID.fromString(value);
            return value.length() == 36 ? null : String.format("\"%s\" UUID has invalid length", value);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String uriTemplateOperator(String value) {
        int level = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '{') {
                level++;
            } else if (c == '}' && level > 0) {
                level--;
            }
        }
        return level == 0 ? null : String.format("\"%s\" is not a valid URI template", value);
    }

    private static String rjpOperator(String value) {
        int firstSegmentEndIdx = value.length();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '#' || c == '/') {
                firstSegmentEndIdx = i;
                break;
            }
            if (c < '0' || c > '9') {
                return invalidRjpMessage(value);
            }
        }
        String firstSegment = value.substring(0, firstSegmentEndIdx);
        String secondSegment = value.substring(firstSegmentEndIdx);
        if (firstSegment.isEmpty() ||
                firstSegment.length() > 1 && firstSegment.startsWith("0") || // no leading zeros
                !"#".equals(secondSegment) && !validateJsonPointer(secondSegment)) {
            return invalidRjpMessage(value);
        }
        return null;
    }

    private static String invalidRjpMessage(String value) {
        return String.format("\"%s\" is not a valid relative-json-pointer", value);
    }
}
