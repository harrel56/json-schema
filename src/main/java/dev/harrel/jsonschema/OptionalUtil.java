package dev.harrel.jsonschema;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

class OptionalUtil {
    private OptionalUtil() {}

    @SafeVarargs
    static <T> Optional<T> firstPresent(Supplier<Optional<T>>... optionals) {
        return Stream.of(optionals)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
