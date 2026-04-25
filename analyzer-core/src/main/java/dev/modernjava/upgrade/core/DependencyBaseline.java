package dev.modernjava.upgrade.core;

import java.util.Objects;

public record DependencyBaseline(
        String category,
        String name,
        String version,
        String evidence) {

    public DependencyBaseline {
        category = normalizeRequired(category, "category");
        name = normalizeRequired(name, "name");
        version = normalizeOptional(version);
        evidence = normalizeOptional(evidence);
    }

    private static String normalizeRequired(String value, String fieldName) {
        var normalized = normalizeOptional(Objects.requireNonNull(value, fieldName));
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
