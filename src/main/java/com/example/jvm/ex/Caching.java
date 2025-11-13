package com.example.jvm.ex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Caching {
//    private static Map<String, Object> cache= new HashMap<>();
//
//    public static void put(String key, Object value) {
//        cache.put(key, value);
//    }

    private static final int MAX_SIZE = 100;
    private static Map<String, Object> cache= new HashMap<>();

    public static void put(String id, Object value) {
        if (cache.size() > MAX_SIZE) {
            String first = cache.keySet().iterator().next();
            cache.remove(first);
        }
        cache.put(id, value);
    }
}
