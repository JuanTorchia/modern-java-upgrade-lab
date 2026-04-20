package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.ProjectMetadata;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class GradleProjectInspector {

    private static final Pattern JAVA_VERSION_CONSTANT = Pattern.compile("JavaVersion\\.VERSION_(\\d+)");
    private static final Pattern JAVA_LANGUAGE_VERSION = Pattern.compile("JavaLanguageVersion\\.of\\((\\d+)\\)");
    private static final Pattern SOURCE_OR_TARGET_COMPATIBILITY = Pattern.compile(
            "(?:sourceCompatibility|targetCompatibility)\\s*=\\s*['\"]?(?:1\\.)?(\\d+)['\"]?");
    private static final Pattern SPRING_BOOT_PLUGIN = Pattern.compile(
            "id\\s*(?:\\(\\s*)?['\"]org\\.springframework\\.boot['\"]\\s*\\)?\\s+version\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern GROOVY_PLUGIN_ID = Pattern.compile("id\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern KOTLIN_PLUGIN_ID = Pattern.compile("id\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)");
    private static final Pattern BARE_JAVA_PLUGIN = Pattern.compile("^\\s*java\\s*$", Pattern.MULTILINE);
    private static final Pattern DEPENDENCY_COORDINATE = Pattern.compile(
            "\\b(?:implementation|api|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\\s*(?:\\(\\s*)?['\"]([^:'\"]+:[^:'\"]+)(?::[^'\"]+)?['\"]");

    public ProjectMetadata inspect(Path projectPath) {
        var buildFile = resolveBuildFile(Objects.requireNonNull(projectPath, "projectPath"));
        var content = readBuildFile(buildFile);

        return new ProjectMetadata(
                "gradle",
                detectJavaVersion(content),
                detectSpringBootVersion(content),
                collectDependencies(content),
                collectBuildPlugins(content));
    }

    private static Path resolveBuildFile(Path projectPath) {
        if (Files.isRegularFile(projectPath) && isGradleBuildFile(projectPath)) {
            return projectPath;
        }

        var kotlinBuild = projectPath.resolve("build.gradle.kts");
        if (Files.isRegularFile(kotlinBuild)) {
            return kotlinBuild;
        }

        var groovyBuild = projectPath.resolve("build.gradle");
        if (Files.isRegularFile(groovyBuild)) {
            return groovyBuild;
        }

        throw new IllegalArgumentException(
                "No Gradle build file found at " + projectPath.toAbsolutePath().normalize());
    }

    private static boolean isGradleBuildFile(Path path) {
        var fileName = path.getFileName().toString();
        return "build.gradle".equals(fileName) || "build.gradle.kts".equals(fileName);
    }

    private static String readBuildFile(Path buildFile) {
        try {
            return Files.readString(buildFile);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read Gradle build file from "
                    + buildFile.toAbsolutePath().normalize(), exception);
        }
    }

    private static String detectJavaVersion(String content) {
        var version = firstMatch(content, JAVA_LANGUAGE_VERSION);
        if (version == null) {
            version = firstMatch(content, JAVA_VERSION_CONSTANT);
        }
        if (version == null) {
            version = firstMatch(content, SOURCE_OR_TARGET_COMPATIBILITY);
        }
        return version == null ? "unknown" : version;
    }

    private static String detectSpringBootVersion(String content) {
        return firstMatch(content, SPRING_BOOT_PLUGIN);
    }

    private static List<String> collectDependencies(String content) {
        return collectMatches(content, DEPENDENCY_COORDINATE);
    }

    private static List<String> collectBuildPlugins(String content) {
        var plugins = new LinkedHashSet<String>();
        plugins.addAll(collectMatches(content, GROOVY_PLUGIN_ID));
        plugins.addAll(collectMatches(content, KOTLIN_PLUGIN_ID));
        if (BARE_JAVA_PLUGIN.matcher(content).find()) {
            plugins.add("java");
        }
        return List.copyOf(plugins);
    }

    private static String firstMatch(String content, Pattern pattern) {
        var matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static List<String> collectMatches(String content, Pattern pattern) {
        Set<String> matches = new LinkedHashSet<>();
        var matcher = pattern.matcher(content);
        while (matcher.find()) {
            matches.add(matcher.group(1).trim());
        }
        return List.copyOf(matches);
    }
}
