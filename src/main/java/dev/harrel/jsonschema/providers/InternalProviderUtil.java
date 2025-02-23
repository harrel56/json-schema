package dev.harrel.jsonschema.providers;

import dev.harrel.jsonschema.JsonNode;

/**
 * Internal helper class.
 */
public final class InternalProviderUtil {
    private InternalProviderUtil() {}

    public static boolean canUseNativeEquals(JsonNode node) {
        return node instanceof AbstractJsonNode<?>;
    }
}
