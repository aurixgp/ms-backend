package com.boutique.common;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {

    private Slugs() {}

    public static String of(String input) {
        String s = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return s.isEmpty() ? "item" : s;
    }

    public static String orGenerate(String slug, String name) {
        return slug == null || slug.isBlank() ? of(name) : of(slug);
    }
}
