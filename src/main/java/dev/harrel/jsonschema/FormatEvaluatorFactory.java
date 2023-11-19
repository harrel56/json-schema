package dev.harrel.jsonschema;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

final class FormatEvaluatorFactory implements EvaluatorFactory {
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "P(?:\\d+W|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}",
            Pattern.CASE_INSENSITIVE
    );

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
                return Optional.of(new EmailFormatEvaluator());
            default:
                return Optional.empty();
        }
    }

    private abstract static class FormatEvaluator implements Evaluator {
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
            return Vocabulary.FORMAT_ASSERTION_VOCABULARY;
        }

        abstract Result evaluateString(String value);
    }

    private static class DateTimeFormatEvaluator extends FormatEvaluator {
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

    private static class DurationFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (DURATION_PATTERN.matcher(value).matches()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid duration string", value));
            }
        }
    }

    private static class EmailFormatEvaluator extends FormatEvaluator {
        @Override
        Result evaluateString(String value) {
            if (EMAIL_PATTERN.matcher(value).matches()) {
                return Result.success();
            } else {
                return Result.failure(String.format("\"%s\" is not a valid email address", value));
            }
        }
    }
}
