package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.util.Objects;

public record InspectorDiagnostic(
        String source,
        InspectorDiagnosticSeverity severity,
        String message,
        Path path) {

    public InspectorDiagnostic {
        source = normalizeRequiredText(source, "source");
        severity = Objects.requireNonNull(severity, "severity");
        message = normalizeRequiredText(message, "message");
    }

    private static String normalizeRequiredText(String value, String fieldName) {
        var normalized = Objects.requireNonNull(value, fieldName)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll(" +", " ")
                .trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
