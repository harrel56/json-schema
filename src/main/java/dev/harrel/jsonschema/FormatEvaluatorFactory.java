package dev.harrel.jsonschema;

import com.sanctionco.jmail.JMail;
import com.sanctionco.jmail.net.InternetProtocolAddress;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;

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
    private static final Pattern URI_TEMPLATE_PATTERN = Pattern.compile("\\{([^/]+?)}");
    private static final Pattern RJP_PATTERN = Pattern.compile("^(\\d+)([^#]*)#?$");

    private final Set<String> vocabularies;

    public FormatEvaluatorFactory() {
        this.vocabularies = emptySet();
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
                return v -> {
                    Matcher matcher = RJP_PATTERN.matcher(v);
                    if (!matcher.find() || matcher.groupCount() != 2) {
                        return String.format("\"%s\" is not a valid relative-json-pointer", v);
                    }
                    String firstSegment = matcher.group(1);
                    if (firstSegment.length() > 1 && firstSegment.startsWith("0") || !validateJsonPointer(matcher.group(2))) {
                        return String.format("\"%s\" is not a valid relative-json-pointer", v);
                    }
                    return null;
                };
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
        String replaced = URI_TEMPLATE_PATTERN.matcher(value).replaceAll("0");
        try {
            URI.create(replaced);
            return null;
        } catch (Exception e) {
            return String.format("\"%s\" is not a valid URI template", value);
        }
    }
}
