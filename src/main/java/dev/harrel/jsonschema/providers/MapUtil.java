package dev.harrel.jsonschema.providers;

import java.util.HashMap;

final class MapUtil {
    private MapUtil() {}

    static <K, V> HashMap<K, V> newHashMap(int realCapacity) {
        return new HashMap<>((int) Math.ceil(realCapacity / 0.75));
    }
}
