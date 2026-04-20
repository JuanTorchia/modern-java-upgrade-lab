package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.util.Objects;

public record SourcePattern(
        SourcePatternType type,
        Path relativePath,
        int lineNumber,
        String evidence) {

    public SourcePattern {
        type = Objects.requireNonNull(type, "type");
        relativePath = Objects.requireNonNull(relativePath, "relativePath");
        if (lineNumber < 1) {
            throw new IllegalArgumentException("lineNumber must be greater than zero");
        }
        evidence = normalize(Objects.requireNonNull(evidence, "evidence"));
    }

    private static String normalize(String value) {
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? value.trim() : normalized;
    }
}
