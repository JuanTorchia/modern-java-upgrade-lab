package dev.modernjava.upgrade.core;

import java.util.Objects;

public record Finding(
        String id,
        FindingCategory category,
        FindingSeverity severity,
        String area,
        String title,
        String evidence,
        String recommendation,
        String openRewriteRecipe) {

    public Finding {
        id = Objects.requireNonNull(id, "id");
        category = Objects.requireNonNull(category, "category");
        severity = Objects.requireNonNull(severity, "severity");
        area = Objects.requireNonNull(area, "area");
        title = normalizeText(Objects.requireNonNull(title, "title"));
        evidence = normalizeText(Objects.requireNonNull(evidence, "evidence"));
        recommendation = normalizeText(Objects.requireNonNull(recommendation, "recommendation"));
        openRewriteRecipe = normalizeOptionalText(openRewriteRecipe);
    }

    private static String normalizeText(String value) {
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? value.trim() : normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
