package dev.harrel.jsonschema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ParameterizedTest(name = "[{index}] {0}: {1}")
@ArgumentsSource(SuiteArgumentsProvider.class)
public @interface SuiteTest {
    String value();
}
