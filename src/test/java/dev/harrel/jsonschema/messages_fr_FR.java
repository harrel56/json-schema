package dev.harrel.jsonschema;

import java.util.ListResourceBundle;

/**
 * Part of ResourceMessageProviderTest
 * it must be public
 */
public class messages_fr_FR extends ListResourceBundle {

    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"type", "type (fr-FR)"},
                {"enum", "enum (fr-FR)"}
        };
    }
}
