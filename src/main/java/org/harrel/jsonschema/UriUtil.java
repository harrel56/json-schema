package org.harrel.jsonschema;

public class UriUtil {

    public static boolean isJsonPoint(String uri) {
        return uri.startsWith("#/");
    }
}
