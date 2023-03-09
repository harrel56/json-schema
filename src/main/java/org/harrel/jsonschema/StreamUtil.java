package org.harrel.jsonschema;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamUtil {

    private StreamUtil() {}

    public static <T> boolean exhaustiveAnyMatch(Stream<T> stream, Predicate<T> predicate) {
        return stream.reduce(Boolean.FALSE, (acc, elem) -> predicate.test(elem) || acc, (b1, b2) -> b1 || b2);
    }

    public static <T> boolean exhaustiveAllMatch(Stream<T> stream, Predicate<T> predicate) {
        return stream.reduce(Boolean.TRUE, (acc, elem) -> predicate.test(elem) && acc, (b1, b2) -> b1 && b2);
    }
}
