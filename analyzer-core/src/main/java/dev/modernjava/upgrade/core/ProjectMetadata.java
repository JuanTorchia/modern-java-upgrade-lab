package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record ProjectMetadata(
        String buildTool,
        String declaredJavaVersion,
        String springBootVersion,
        List<String> dependencies,
        List<String> buildPlugins) {

    public ProjectMetadata {
        buildTool = Objects.requireNonNull(buildTool, "buildTool");
        declaredJavaVersion = Objects.requireNonNull(declaredJavaVersion, "declaredJavaVersion");
        springBootVersion = normalizeOptionalText(springBootVersion);
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        buildPlugins = List.copyOf(Objects.requireNonNull(buildPlugins, "buildPlugins"));
    }

    public ProjectMetadata(String buildTool, String declaredJavaVersion, String springBootVersion,
            List<String> dependencies) {
        this(buildTool, declaredJavaVersion, springBootVersion, dependencies, List.of());
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
