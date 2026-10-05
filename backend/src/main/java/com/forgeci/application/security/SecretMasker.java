package com.forgeci.application.security;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 19.4 — mask secrets before durable log storage.
 * Example: {@code MY_TOKEN=abc123} becomes {@code MY_TOKEN=********}.
 */
public final class SecretMasker {
    private static final Pattern ASSIGNMENT = Pattern.compile(
            "(?i)\\b([A-Z][A-Z0-9_]{1,64})\\s*[=:]\\s*([^\\s\"']+)");

    private SecretMasker() {}

    public static String mask(String text, Collection<String> secrets) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String out = text;
        if (secrets != null) {
            for (String secret : secrets) {
                if (secret != null && !secret.isBlank() && secret.length() >= 3) {
                    out = out.replace(secret, "********");
                }
            }
        }
        // Also redact common assignment forms so KEY=value never persists.
        Matcher m = ASSIGNMENT.matcher(out);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            if (looksSensitive(key)) {
                m.appendReplacement(sb, Matcher.quoteReplacement(key + "=********"));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean looksSensitive(String key) {
        String k = key.toUpperCase();
        return k.contains("TOKEN")
                || k.contains("SECRET")
                || k.contains("PASSWORD")
                || k.contains("PASSWD")
                || k.contains("API_KEY")
                || k.contains("APIKEY")
                || k.contains("ACCESS_KEY")
                || k.contains("PRIVATE_KEY")
                || k.contains("CREDENTIAL")
                || k.endsWith("_KEY")
                || k.endsWith("_SECRET");
    }
}
