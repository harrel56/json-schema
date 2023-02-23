package org.harrel.jsonschema;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UriUtil {

    private UriUtil() {}

    public static boolean isJsonPointerOrAnchor(String uri) {
        return uri.startsWith("#") && uri.length() > 1;
    }

    public static String resolveUri(URI baseUri, String ref) {
        ref = UriUtil.decodeUrl(ref);
        if (baseUri.getAuthority() == null && UriUtil.isJsonPointerOrAnchor(ref)) {
            return baseUri + ref;
        }
        if (ref.equals("#")) {
            return baseUri.toString();
        } else if (UriUtil.isJsonPointerOrAnchor(ref)) {
            return baseUri + ref;
        } else {
            return baseUri.resolve(ref).toString();
        }
    }

    public static String decodeUrl(String url) {
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String[] split = decoded.split("#", -1);
        StringBuilder sb = new StringBuilder(split[0]);
        if (split.length > 1) {
            sb.append('#');
            sb.append(decodeJsonPointer(split[1]));
        }
        return sb.toString();
    }

    public static String decodeJsonPointer(String pointer) {
        return pointer.replace("~0", "~").replace("~1", "/");
    }
}
