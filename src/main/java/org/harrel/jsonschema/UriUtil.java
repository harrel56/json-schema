package org.harrel.jsonschema;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class UriUtil {

    private UriUtil() {}

    public static Optional<String> getAnchor(String uri) {
        return Optional.ofNullable(URI.create(uri).getFragment())
                .filter(fragment -> !fragment.startsWith("/"));
    }

    public static String getUriWithoutFragment(String uri) {
        int fragmentIdx = uri.indexOf('#');
        if (fragmentIdx < 0) {
            return uri;
        } else {
            return uri.substring(0, fragmentIdx);
        }
    }

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
