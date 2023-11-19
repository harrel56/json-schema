package dev.harrel.jsonschema;

import com.sanctionco.jmail.JMail;
import com.sanctionco.jmail.net.InternetProtocolAddress;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;

public final class FormatEvaluatorFactory implements EvaluatorFactory {
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "P(?:\\d+W|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    private final Set<String> vocabularies;

    public FormatEvaluatorFactory() {
        this.vocabularies = emptySet();
    }

    @Override
    public Optional<Evaluator> create(SchemaParsingContext parsingCtx, String fieldName, JsonNode fieldNode) {
        if (!"format".equals(fieldName) || !fieldNode.isString()) {
            return Optional.empty();
        }

        switch (fieldNode.asString()) {
            case "date":
                return Optional.of(new DateTimeFormatEvaluator(DateTimeFormatter.ISO_DATE));
            case "date-time":
                return Optional.of(new DateTimeFormatEvaluator(DateTimeFormatter.ISO_DATE_TIME));
            case "time":
                return Optional.of(new DateTimeFormatEvaluator(DateTimeFormatter.ISO_TIME));
            case "duration":
                return Optional.of(new DurationFormatEvaluator());
            case "email":
            case "idn-email":
                return Optional.of(new EmailFormatEvaluator());
            case "hostname":
            case "idn-hostname":
                return Optional.of(new HostnameFormatEvaluator());
            case "ipv4":
                return Optional.of(new Ipv4FormatEvaluator());
            case "ipv6":
                return Optional.of(new Ipv6FormatEvaluator());
            case "uri":
            case "iri":
                return Optional.of(new UriFormatEvaluator());
            case "uri-reference":
            case "iri-reference":
                return Optional.of(new UriReferenceFormatEvaluator());
            case "uuid":
                return Optional.of(new UuidFormatEvaluator());
            default:
                return Optional.empty();
        }
    }

    private abstract class FormatEvaluator implements Evaluator {
        @Override
        public final Result evaluate(EvaluationContext ctx, JsonNode node) {
            if (node.isString()) {
                return evaluateString(node.asString());
            } else {
                return Result.success();
            }
        }

        @Override
        public Set<String> getVocabularies() {
            return vocabularies;
        }

        abstract Result evaluateString(String value);
    }

    private class DateTimeFormatEvaluator extends FormatEvaluator {
        private final DateTimeFormatter formatter;

        public DateTimeFormatEvaluator(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        Result evaluateString(String value) {
            try {
                formatter.parse(value);
                return Result.success();
            } catch (DateTimeParseException e) {
                return Result.failure(e.getMessage());
            }
        }
    }

    private class DurationFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (DURATION_PATTERN.matcher(value).matches()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid duration string", value));
            }
        }
    }

    private class EmailFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (JMail.isValid(value)) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid email address", value));
            }
        }
    }

    private class HostnameFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (HOSTNAME_PATTERN.matcher(value).matches()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid hostname", value));
            }
        }
    }

    private class Ipv4FormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (InternetProtocolAddress.validateIpv4(value).isPresent()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid ipv4 address", value));
            }
        }
    }

    private class Ipv6FormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (InternetProtocolAddress.validateIpv6(value).isPresent()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid ipv6 address", value));
            }
        }
    }

    private class UriFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            try {
                URI uri = new URI(value);
                if (uri.isAbsolute()) {
                    return Result.success();
                } else {
                    return Result.failure(String.format("[%s] is a relative URI", uri));
                }
            } catch (URISyntaxException e) {
                return Result.failure(e.getMessage());
            }
        }
    }

    private class UriReferenceFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            try {
                new URI(value);
                return Result.success();
            } catch (URISyntaxException e) {
                return Result.failure(e.getMessage());
            }
        }
    }

    private class UuidFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            try {
                UUID.fromString(value);
                if (value.length() == 36) {
                    return Result.success();
                } else {
                    return Result.failure(String.format("[%s] UUID has invalid length", value));
                }
            } catch (Exception e) {
                return Result.failure(e.getMessage());
            }
        }
    }
}
