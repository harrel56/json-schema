package dev.harrel.jsonschema;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Because fragments can contain illegal characters and URI parsing might fail.
 */
final class CompoundUri {
    final URI uri;
    final String fragment;

    static CompoundUri fromUri(URI uri) {
        if (uri.getRawFragment() == null) {
            return new CompoundUri(uri, "");
        } else {
            return fromString(uri.toString());
        }
    }

    static CompoundUri fromString(String ref) {
        String[] split = ref.split("#", -1);
        String fragment = split.length > 1 ? split[1] : "";
        return new CompoundUri(URI.create(split[0]), fragment);
    }

    CompoundUri(URI uri, String fragment) {
        this.uri = Objects.requireNonNull(uri);
        this.fragment = Objects.requireNonNull(fragment);
    }

    @Override
    public String toString() {
        return fragment.isEmpty() ? uri.toString() : uri + "#" + fragment;
    }
}

final class UriUtil {
    private UriUtil() {}

    static URI getUriWithoutFragment(URI uri) {
        if (uri.getRawFragment() == null) {
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

    static URI removeEmptyFragment(String uri) {
        if (uri.endsWith("#")) {
            return URI.create(uri.substring(0, uri.length() - 1));
        } else {
            return URI.create(uri);
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

    static CompoundUri resolveUri(URI baseUri, CompoundUri ref) {
        String fragment = urlDecode(ref.fragment);
        if (ref.uri.toString().isEmpty()) {
            return new CompoundUri(baseUri, fragment);
        } else {
            return new CompoundUri(baseUri.resolve(ref.uri), fragment);
        }
    }

    private static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
