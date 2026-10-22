package com.movieweb.util;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Presentation helpers: never render database values as trusted markup or URLs. */
public final class ViewUtils {
    private ViewUtils() { }
    public static String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
    public static String text(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
    public static String query(Object value) {
        return URLEncoder.encode(value == null ? "" : String.valueOf(value), StandardCharsets.UTF_8);
    }

    /* Accept an absolute HTTP(S) URL only; reject credentials, controls and other schemes. */
    public static String externalUrl(String value) {
        if (!hasText(value)) return "";
        String url = value.trim();
        if (url.chars().anyMatch(c -> c <= 32 || c == 127 || c == '\\')) return "";
        try {
            URI parsed = URI.create(url);
            String scheme = parsed.getScheme();
            if (("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && parsed.getHost() != null && parsed.getUserInfo() == null) return url;
        } catch (IllegalArgumentException ignored) { }
        return "";
    }

    /* Local image paths and HTTP(S) images are supported. Unsafe paths use a real placeholder. */
    public static String asset(String contextPath, String value) {
        String fallback = contextPath + "/pictures/movie-placeholder.svg";
        if (!hasText(value)) return fallback;
        String remote = externalUrl(value);
        if (!remote.isEmpty()) return remote;
        String path = value.trim();
        if (path.startsWith("//") || path.indexOf('\\') >= 0 || path.indexOf(':') >= 0
                || path.chars().anyMatch(c -> c < 32 || c == 127 || c == '?' || c == '#' || c == '%')) return fallback;
        while (path.startsWith("/")) path = path.substring(1);
        if (path.isEmpty()) return fallback;
        for (String part : path.split("/")) {
            if (part.equals(".") || part.equals("..")) return fallback;
        }
        return contextPath + "/" + path;
    }
    public static String date(Timestamp value) {
        return value == null ? "Not available" : value.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public static String dateTime(Timestamp value) {
        return value == null ? "Not available" : value.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    public static String rating(double value) {
        return Double.isFinite(value) && value > 0
                ? String.format(Locale.ROOT, "%.1f / 10", value) : "Not rated yet";
    }
}