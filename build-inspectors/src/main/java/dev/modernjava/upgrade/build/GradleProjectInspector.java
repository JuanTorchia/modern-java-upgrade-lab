package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.DependencyBaseline;
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
    private static final Pattern GROOVY_PLUGIN_WITH_VERSION = Pattern.compile(
            "id\\s+['\"]([^'\"]+)['\"]\\s+version\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern KOTLIN_PLUGIN_WITH_VERSION = Pattern.compile(
            "id\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)\\s+version\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern GROOVY_PLUGIN_ID = Pattern.compile("id\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern KOTLIN_PLUGIN_ID = Pattern.compile("id\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)");
    private static final Pattern BARE_JAVA_PLUGIN = Pattern.compile("^\\s*java\\s*$", Pattern.MULTILINE);
    private static final Pattern DEPENDENCY_COORDINATE = Pattern.compile(
            "\\b(?:implementation|api|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\\s*(?:\\(\\s*)?['\"]([^:'\"]+:[^:'\"]+)(?::[^'\"]+)?['\"]");
    private static final Pattern DEPENDENCY_CATALOG_REFERENCE = Pattern.compile(
            "\\b(?:implementation|api|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\\s*(?:\\(\\s*)?"
                    + "(libs(?:\\.[A-Za-z][A-Za-z0-9_-]*)+)\\b");
    private static final Pattern DEPENDENCY_WITH_VERSION = Pattern.compile(
            "\\b(implementation|api|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\\s*(?:\\(\\s*)?"
                    + "['\"]([^:'\"]+:[^:'\"]+):([^'\"]+)['\"]");
    static final Pattern PLUGIN_CATALOG_REFERENCE = Pattern.compile(
            "\\balias\\s*(?:\\(\\s*)?(libs\\.plugins(?:\\.[A-Za-z][A-Za-z0-9_-]*)+)\\b");
    private static final Pattern JIB_FROM_IMAGE = Pattern.compile(
            "\\bfrom\\s*\\{.*?\\bimage\\s*=\\s*['\"]([^'\"]+)['\"].*?\\}",
            Pattern.DOTALL);
    private static final Pattern GRADLE_DISTRIBUTION = Pattern.compile("gradle-(\\d+(?:\\.\\d+)+)-");
    private static final Pattern COMPILER_ARGS_LIST = Pattern.compile(
            "compilerArgs\\s*(?:=|\\+=)\\s*(?:listOf\\s*\\(|\\[)(.*?)(?:\\)|\\])",
            Pattern.DOTALL);
    private static final Pattern COMPILER_ARGS_METHOD = Pattern.compile(
            "compilerArgs\\.(?:add|addAll)\\s*\\((.*?)\\)",
            Pattern.DOTALL);
    private static final Pattern QUOTED_STRING = Pattern.compile("['\"]([^'\"]+)['\"]");

    public ProjectMetadata inspect(Path projectPath) {
        var buildFile = resolveBuildFile(Objects.requireNonNull(projectPath, "projectPath"));
        var content = readBuildFile(buildFile);
        var projectRoot = resolveProjectRoot(projectPath, buildFile);
        var catalog = GradleVersionCatalog.read(projectRoot);

        return new ProjectMetadata(
                "gradle",
                detectJavaVersion(content),
                detectSpringBootVersion(content, catalog),
                collectDependencies(content, catalog),
                collectBuildPlugins(content, catalog),
                collectCompilerArgs(content),
                List.of(),
                collectDependencyBaselines(content, projectRoot, buildFile.getFileName().toString()),
                catalog.diagnostics());
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

    private static Path resolveProjectRoot(Path projectPath, Path buildFile) {
        if (Files.isRegularFile(projectPath) && isGradleBuildFile(projectPath)) {
            return buildFile.toAbsolutePath().normalize().getParent();
        }
        return projectPath.toAbsolutePath().normalize();
    }

    private static String readBuildFile(Path buildFile) {
        return readText(buildFile);
    }

    private static String readText(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read Gradle file from "
                    + path.toAbsolutePath().normalize(), exception);
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

    private static String detectSpringBootVersion(String content, GradleVersionCatalog catalog) {
        var directVersion = firstMatch(content, SPRING_BOOT_PLUGIN);
        return directVersion == null ? catalog.springBootVersion(content) : directVersion;
    }

    private static List<String> collectDependencies(String content, GradleVersionCatalog catalog) {
        var dependencies = new LinkedHashSet<>(collectMatches(content, DEPENDENCY_COORDINATE));
        var matcher = DEPENDENCY_CATALOG_REFERENCE.matcher(content);
        while (matcher.find()) {
            catalog.addDependencies(matcher.group(1), dependencies);
        }
        return List.copyOf(dependencies);
    }

    private static List<String> collectBuildPlugins(String content, GradleVersionCatalog catalog) {
        var plugins = new LinkedHashSet<String>();
        plugins.addAll(collectMatches(content, GROOVY_PLUGIN_ID));
        plugins.addAll(collectMatches(content, KOTLIN_PLUGIN_ID));
        if (BARE_JAVA_PLUGIN.matcher(content).find()) {
            plugins.add("java");
        }
        var matcher = PLUGIN_CATALOG_REFERENCE.matcher(content);
        while (matcher.find()) {
            catalog.addPlugin(matcher.group(1), plugins);
        }
        return List.copyOf(plugins);
    }

    private static List<String> collectCompilerArgs(String content) {
        var uncommentedContent = stripComments(content);
        var compilerArgs = new LinkedHashSet<String>();
        collectQuotedMatches(uncommentedContent, COMPILER_ARGS_LIST, compilerArgs);
        collectQuotedMatches(uncommentedContent, COMPILER_ARGS_METHOD, compilerArgs);
        return List.copyOf(compilerArgs);
    }

    private static List<DependencyBaseline> collectDependencyBaselines(
            String content, Path projectRoot, String buildFileName) {
        var baselines = new LinkedHashSet<DependencyBaseline>();
        addGradleWrapperBaseline(projectRoot, baselines);
        addPluginBaselines(content, buildFileName, baselines);
        addDependencyVersionBaselines(content, buildFileName, baselines);
        addJibBaseImageBaseline(content, buildFileName, baselines);
        return List.copyOf(baselines);
    }

    private static void addGradleWrapperBaseline(Path projectRoot, Set<DependencyBaseline> baselines) {
        var wrapperPath = projectRoot.resolve("gradle").resolve("wrapper").resolve("gradle-wrapper.properties");
        if (!Files.isRegularFile(wrapperPath)) {
            return;
        }

        var matcher = GRADLE_DISTRIBUTION.matcher(readText(wrapperPath));
        if (matcher.find()) {
            baselines.add(new DependencyBaseline(
                    "Build tool",
                    "Gradle wrapper",
                    matcher.group(1),
                    "gradle/wrapper/gradle-wrapper.properties"));
        }
    }

    private static void addPluginBaselines(String content, String evidence, Set<DependencyBaseline> baselines) {
        addPluginBaselines(content, evidence, baselines, GROOVY_PLUGIN_WITH_VERSION);
        addPluginBaselines(content, evidence, baselines, KOTLIN_PLUGIN_WITH_VERSION);
    }

    private static void addPluginBaselines(
            String content, String evidence, Set<DependencyBaseline> baselines, Pattern pattern) {
        var matcher = pattern.matcher(content);
        while (matcher.find()) {
            baselines.add(new DependencyBaseline(
                    "Build plugin",
                    matcher.group(1),
                    matcher.group(2),
                    evidence));
        }
    }

    private static void addDependencyVersionBaselines(String content, String evidence, Set<DependencyBaseline> baselines) {
        var matcher = DEPENDENCY_WITH_VERSION.matcher(content);
        while (matcher.find()) {
            baselines.add(new DependencyBaseline(
                    dependencyBaselineCategory(matcher.group(1)),
                    matcher.group(2),
                    matcher.group(3),
                    evidence));
        }
    }

    private static String dependencyBaselineCategory(String configuration) {
        if (configuration.startsWith("test")) {
            return "Test dependency";
        }
        if ("runtimeOnly".equals(configuration)) {
            return "Runtime dependency";
        }
        return "Application dependency";
    }

    private static void addJibBaseImageBaseline(String content, String evidence, Set<DependencyBaseline> baselines) {
        var matcher = JIB_FROM_IMAGE.matcher(content);
        if (matcher.find()) {
            baselines.add(new DependencyBaseline(
                    "Runtime image",
                    "Jib base image",
                    matcher.group(1),
                    evidence));
        }
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

    private static void collectQuotedMatches(String content, Pattern statementPattern, Set<String> matches) {
        var statementMatcher = statementPattern.matcher(content);
        while (statementMatcher.find()) {
            var quotedMatcher = QUOTED_STRING.matcher(statementMatcher.group(1));
            while (quotedMatcher.find()) {
                matches.add(quotedMatcher.group(1).trim());
            }
        }
    }

    private static String stripComments(String content) {
        var stripped = new StringBuilder(content.length());
        var inBlockComment = false;
        char quote = 0;
        var escaped = false;
        for (int index = 0; index < content.length(); index++) {
            var current = content.charAt(index);
            var next = index + 1 < content.length() ? content.charAt(index + 1) : '\0';

            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    inBlockComment = false;
                    index++;
                } else if (current == '\n') {
                    stripped.append(current);
                }
                continue;
            }

            if (quote != 0) {
                stripped.append(current);
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote) {
                    quote = 0;
                }
                continue;
            }

            if (current == '"' || current == '\'') {
                quote = current;
                stripped.append(current);
            } else if (current == '/' && next == '/') {
                while (index < content.length() && content.charAt(index) != '\n') {
                    index++;
                }
                if (index < content.length()) {
                    stripped.append(content.charAt(index));
                }
            } else if (current == '/' && next == '*') {
                inBlockComment = true;
                index++;
            } else {
                stripped.append(current);
            }
        }
        return stripped.toString();
    }
}
