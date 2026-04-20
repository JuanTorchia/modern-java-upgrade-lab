package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record ProjectMetadata(
        String buildTool,
        String declaredJavaVersion,
        String springBootVersion,
        List<String> dependencies) {

    public ProjectMetadata {
        buildTool = Objects.requireNonNull(buildTool, "buildTool");
        declaredJavaVersion = Objects.requireNonNull(declaredJavaVersion, "declaredJavaVersion");
        springBootVersion = normalizeOptionalText(springBootVersion);
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
