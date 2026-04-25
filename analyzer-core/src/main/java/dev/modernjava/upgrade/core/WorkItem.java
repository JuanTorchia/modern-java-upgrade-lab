package dev.modernjava.upgrade.core;

import java.util.Objects;

public record WorkItem(
        String id,
        String category,
        String title,
        String rationale,
        String command,
        String priority,
        String phase) {

    public WorkItem {
        id = normalizeRequired(id, "id");
        category = normalizeRequired(category, "category");
        title = normalizeRequired(title, "title");
        rationale = normalizeRequired(rationale, "rationale");
        command = normalizeOptional(command);
        priority = normalizeOptional(priority);
        if (priority == null) {
            priority = "P1";
        }
        phase = normalizeOptional(phase);
        if (phase == null) {
            phase = category;
        }
    }

    public WorkItem(String id, String category, String title, String rationale, String command) {
        this(id, category, title, rationale, command, "P1", category);
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
