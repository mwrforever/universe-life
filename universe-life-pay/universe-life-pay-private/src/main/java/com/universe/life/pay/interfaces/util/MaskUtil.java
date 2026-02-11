package com.universe.life.pay.interfaces.util;

public class MaskUtil {

    private MaskUtil() {
    }

    public static String mask(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String s = raw.trim();
        if (s.length() <= 6) {
            return "****";
        }
        int prefix = 3;
        int suffix = 3;
        if (s.length() < prefix + suffix + 1) {
            return "****";
        }
        return s.substring(0, prefix) + "****" + s.substring(s.length() - suffix);
    }
}
