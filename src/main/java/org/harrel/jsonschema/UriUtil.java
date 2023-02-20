package org.harrel.jsonschema;

public class UriUtil {

    public static boolean isRelativeJsonPoint(String uri) {
        return uri.startsWith("#/");
    }
}
