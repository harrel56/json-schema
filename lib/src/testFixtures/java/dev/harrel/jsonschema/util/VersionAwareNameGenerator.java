package dev.harrel.jsonschema.util;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.util.Optional;

public class VersionAwareNameGenerator extends DisplayNameGenerator.Standard {
    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        /* IJ is broken and doesn't handle dots + it doesn't even display the names properly */
        String version = Optional.ofNullable(System.getenv("PROVIDER_VERSION"))
                .map(v -> v.replace('.', '_'))
                .map(" (%s)"::formatted)
                .orElse("");
        return super.generateDisplayNameForClass(testClass) + version;
    }
}
