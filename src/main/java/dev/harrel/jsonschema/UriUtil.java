package dev.harrel.jsonschema;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

final class UriUtil {

    private UriUtil() {}

    static boolean hasNonEmptyFragment(URI uri) {
        return uri.getFragment() != null && !uri.getFragment().isEmpty();
    }

    static Optional<String> getAnchor(String uri) {
        return Optional.ofNullable(URI.create(uri).getFragment())
                .filter(fragment -> !fragment.startsWith("/"));
    }

    static URI getUriWithoutFragment(URI uri) {
        if (uri.getFragment() == null) {
            return uri;
        } else {
            return getUriWithoutFragment(uri.toString());
        }
    }

    static URI getUriWithoutFragment(String uri) {
        int fragmentIdx = uri.indexOf('#');
        if (fragmentIdx < 0) {
            return URI.create(uri);
        } else {
            return URI.create(uri.substring(0, fragmentIdx));
        }
    }

    static String getJsonPointer(String uri) {
        int fragmentIdx = uri.indexOf('#');
        if (fragmentIdx < 0) {
            return "";
        } else {
            return uri.substring(fragmentIdx + 1);
        }
    }

    static String getJsonPointerParent(String pointer) {
        return pointer.substring(0, pointer.lastIndexOf('/'));
    }

    static boolean isJsonPointerOrAnchor(String uri) {
        return uri.startsWith("#") && uri.length() > 1;
    }

    static String resolveUri(URI baseUri, String ref) {
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

    static String decodeUrl(String url) {
        String[] split = url.split("#", -1);
        StringBuilder sb = new StringBuilder(split[0]);
        if (split.length > 1) {
            sb.append('#');
            sb.append(decodeJsonPointer(split[1]));
        }
        return sb.toString();
    }

    static String decodeJsonPointer(String pointer) {
        return internalDecode(pointer).replace("~0", "~").replace("~1", "/");
    }

    private static String internalDecode(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
