package dev.harrel.jsonschema;

import com.sanctionco.jmail.JMail;
import com.sanctionco.jmail.net.InternetProtocolAddress;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

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
    private final Predicate<SchemaParsingContext> vocabPredicate;

    /**
     * Creates a default instance without vocabularies support.
     */
    public FormatEvaluatorFactory() {
        this.vocabPredicate = ctx -> true;
    }

    /**
     * Creates a customized instance with vocabularies support.
     * Validation will only be run when at least one of provided vocabularies is active during validation process.
     */
    public FormatEvaluatorFactory(Set<String> vocabularies) {
        Set<String> vocabsCopy = unmodifiableSet(new HashSet<>(vocabularies));
        this.vocabPredicate = ctx -> !Collections.disjoint(vocabsCopy, ctx.getActiveVocabularies());
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext ctx, String fieldName, JsonNode fieldNode) {
        if (!"format".equals(fieldName) || !fieldNode.isString() || !vocabPredicate.test(ctx)) {
            return Optional.empty();
        }
        return Optional.of(new FormatEvaluator(fieldNode.asString()));
    }

    private static final class FormatException extends Exception {
        FormatException() {
            this(null);
        }

        FormatException(String message) {
            super(message, null, false, false);
        }
    }

    @FunctionalInterface
    private interface FormatOperator {
        void validateFormat(String value) throws FormatException;
    }

    private static final class FormatEvaluator implements Evaluator {
        private static final Pattern DURATION_PATTERN = Pattern.compile(
                "P(?:\\d+W|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?)",
                Pattern.CASE_INSENSITIVE
        );
        private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
                "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*",
                Pattern.CASE_INSENSITIVE
        );

        private final String format;
        private final FormatOperator operator;

        private FormatEvaluator(String format) {
            this.format = format;
            this.operator = getOperator(format);
        }

        @Override
        public Result evaluate(EvaluationContext ctx, JsonNode node) {
            if (!node.isString()) {
                return Result.success();
            }
            String value = node.asString();
            try {
                operator.validateFormat(value);
                return Result.success(value);
            } catch (FormatException e) {
                String details = e.getMessage() == null ? "" : e.getMessage();
                return Result.formattedFailure("format", value, format, details.length(), details);
            }
        }

        private static FormatOperator getOperator(String format) {
            switch (format) {
                case "date":
                    return tryOf(DateTimeFormatter.ISO_DATE::parse);
                case "date-time":
                    return tryOf(DateTimeFormatter.ISO_DATE_TIME::parse);
                case "time":
                    return tryOf(DateTimeFormatter.ISO_TIME::parse);
                case "duration":
                    return predicateOf(v -> DURATION_PATTERN.matcher(v).matches());
                case "email":
                case "idn-email":
                    return predicateOf(JMail::isValid);
                case "hostname":
                case "idn-hostname":
                    return predicateOf(v -> HOSTNAME_PATTERN.matcher(v).matches());
                case "ipv4":
                    return predicateOf(v -> InternetProtocolAddress.validateIpv4(v).isPresent());
                case "ipv6":
                    return predicateOf(v -> InternetProtocolAddress.validateIpv6(v).isPresent());
                case "uri":
                    return FormatEvaluator::uriOperator;
                case "iri":
                    return FormatEvaluator::iriOperator;
                case "uri-reference":
                    return FormatEvaluator::uriReferenceOperator;
                case "iri-reference":
                    return tryOf(URI::create);
                case "uuid":
                    return FormatEvaluator::uuidOperator;
                case "uri-template":
                    return predicateOf(FormatEvaluator::validateUriTemplate);
                case "json-pointer":
                    return predicateOf(FormatEvaluator::validateJsonPointer);
                case "relative-json-pointer":
                    return predicateOf(FormatEvaluator::validateRjp);
                case "regex":
                    return tryOf(Pattern::compile);
                default:
                    return v -> {};
            }
        }

        private static FormatOperator tryOf(Consumer<String> op) {
            return v -> {
                try {
                    op.accept(v);
                } catch (RuntimeException e) {
                    throw new FormatException(e.getMessage());
                }
            };
        }

        private static FormatOperator predicateOf(Predicate<String> predicate) {
            return v -> {
                if (!predicate.test(v)) {
                    throw new FormatException();
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

        private static void uriOperator(String value) throws FormatException {
            asciiOperator(value);
            iriOperator(value);
        }

        private static void iriOperator(String value) throws FormatException {
            try {
                if (!URI.create(value).isAbsolute()) {
                    throw new FormatException(String.format("\"%s\" is relative", value));
                }
            } catch (RuntimeException e) {
                throw new FormatException(e.getMessage());
            }
        }

        private static void uriReferenceOperator(String value) throws FormatException {
            asciiOperator(value);
            try {
                URI.create(value);
            } catch (RuntimeException e) {
                throw new FormatException(e.getMessage());
            }
        }

        private static void uuidOperator(String value) throws FormatException {
            try {
                if (!UUID.fromString(value).toString().equalsIgnoreCase(value)) {
                    throw new FormatException();
                }
            } catch (RuntimeException e) {
                throw new FormatException(e.getMessage());
            }
        }

        private static boolean validateUriTemplate(String value) {
            int level = 0;
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '{') {
                    level++;
                } else if (c == '}' && level > 0) {
                    level--;
                }
            }
            return level == 0;
        }

        private static boolean validateRjp(String value) {
            int firstSegmentEndIdx = value.length();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '#' || c == '/') {
                    firstSegmentEndIdx = i;
                    break;
                }
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            String firstSegment = value.substring(0, firstSegmentEndIdx);
            String secondSegment = value.substring(firstSegmentEndIdx);
            if (firstSegment.isEmpty() ||
                    firstSegment.length() > 1 && firstSegment.startsWith("0") || // no leading zeros
                    !"#".equals(secondSegment) && !validateJsonPointer(secondSegment)) {
                return false;
            }
            return true;
        }

        private static void asciiOperator(String value) throws FormatException {
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) >= '\u0080') {
                    throw new FormatException(String.format("\"%s\" contains non-ASCII characters", value));
                }
            }
        }
    }
}
