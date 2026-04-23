package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record ProjectMetadata(
        String buildTool,
        String declaredJavaVersion,
        String springBootVersion,
        List<String> dependencies,
        List<String> buildPlugins,
        List<String> compilerArgs,
        List<SourcePattern> sourcePatterns,
        List<InspectorDiagnostic> diagnostics) {

    public ProjectMetadata {
        buildTool = Objects.requireNonNull(buildTool, "buildTool");
        declaredJavaVersion = Objects.requireNonNull(declaredJavaVersion, "declaredJavaVersion");
        springBootVersion = normalizeOptionalText(springBootVersion);
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        buildPlugins = List.copyOf(Objects.requireNonNull(buildPlugins, "buildPlugins"));
        compilerArgs = List.copyOf(Objects.requireNonNull(compilerArgs, "compilerArgs"));
        sourcePatterns = List.copyOf(Objects.requireNonNull(sourcePatterns, "sourcePatterns"));
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
    }

    public ProjectMetadata(String buildTool, String declaredJavaVersion, String springBootVersion,
            List<String> dependencies, List<String> buildPlugins) {
        this(buildTool, declaredJavaVersion, springBootVersion, dependencies, buildPlugins, List.of(), List.of(), List.of());
    }

    public ProjectMetadata(String buildTool, String declaredJavaVersion, String springBootVersion,
            List<String> dependencies, List<String> buildPlugins, List<SourcePattern> sourcePatterns) {
        this(buildTool, declaredJavaVersion, springBootVersion, dependencies, buildPlugins, List.of(), sourcePatterns, List.of());
    }

    public ProjectMetadata(String buildTool, String declaredJavaVersion, String springBootVersion,
            List<String> dependencies, List<String> buildPlugins, List<String> compilerArgs,
            List<SourcePattern> sourcePatterns) {
        this(buildTool, declaredJavaVersion, springBootVersion, dependencies, buildPlugins, compilerArgs, sourcePatterns, List.of());
    }

    public ProjectMetadata(String buildTool, String declaredJavaVersion, String springBootVersion,
            List<String> dependencies) {
        this(buildTool, declaredJavaVersion, springBootVersion, dependencies, List.of(), List.of(), List.of(), List.of());
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
