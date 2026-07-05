
package dev.harrel.jsonschema.internal;

import dev.harrel.jsonschema.JsonNode;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Internal helper class.
 */
public final class InternalProviderUtil {
    private InternalProviderUtil() {}

    public static boolean canUseNativeEquals(JsonNode n1, JsonNode n2) {
        return n1 instanceof StandaloneNode && n2 instanceof StandaloneNode ||
                n1 instanceof AbstractJsonNode<?> && n2 instanceof AbstractJsonNode<?>;
    }

    public static boolean canConvertToInteger(BigDecimal bigDecimal) {
        return bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0;
    }

    public static <K, V> HashMap<K, V> newHashMap(int realCapacity) {
        return new HashMap<>((int) Math.ceil(realCapacity / 0.75));
    }
}
