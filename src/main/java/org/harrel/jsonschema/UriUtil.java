package org.harrel.jsonschema;

public class UriUtil {

    private UriUtil() {}

    public static boolean isRelativeJsonPoint(String uri) {
        return uri.startsWith("#/");
    }
}
