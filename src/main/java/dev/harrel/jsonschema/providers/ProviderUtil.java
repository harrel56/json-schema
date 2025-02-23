package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;

/**
 * Internal helper class.
 */
public final class ProviderUtil {
    private ProviderUtil() {}

    public static boolean canUseNativeEquals(JsonNode node) {
        return node instanceof AbstractJsonNode<?>;
    }
}
