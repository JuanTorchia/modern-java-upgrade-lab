package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.DependencyBaseline;
import dev.modernjava.upgrade.core.ProjectMetadata;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public final class MavenProjectInspector {

    private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";

    public ProjectMetadata inspect(Path projectPath) {
        var pomPath = resolvePomPath(Objects.requireNonNull(projectPath, "projectPath"));
        var models = readModelTree(pomPath);

        return new ProjectMetadata(
                "maven",
                detectJavaVersion(models),
                detectSpringBootVersion(models),
                collectDependencies(models),
                collectBuildPlugins(models),
                collectCompilerArgs(models),
                List.of(),
                collectDependencyBaselines(models),
                List.of());
    }

    private static Path resolvePomPath(Path projectPath) {
        if (Files.isRegularFile(projectPath) && "pom.xml".equals(projectPath.getFileName().toString())) {
            return projectPath;
        }

        var pomPath = projectPath.resolve("pom.xml");
        if (Files.isRegularFile(pomPath)) {
            return pomPath;
        }

        throw new IllegalArgumentException("No pom.xml found at " + projectPath.toAbsolutePath().normalize());
    }

    private static Model readModel(Path pomPath) {
        try (Reader reader = Files.newBufferedReader(pomPath)) {
            return new MavenXpp3Reader().read(reader);
        } catch (IOException | XmlPullParserException exception) {
            throw new IllegalStateException("Failed to read Maven model from " + pomPath.toAbsolutePath().normalize(),
                    exception);
        }
    }

    private static List<Model> readModelTree(Path rootPomPath) {
        var models = new ArrayList<Model>();
        var rootModel = readModel(rootPomPath);
        models.add(rootModel);

        var rootDirectory = rootPomPath.toAbsolutePath().normalize().getParent();
        if (rootDirectory == null || rootModel.getModules() == null) {
            return List.copyOf(models);
        }

        for (String module : rootModel.getModules()) {
            if (module == null || module.isBlank()) {
                continue;
            }
            var modulePom = rootDirectory.resolve(module.trim()).resolve("pom.xml").normalize();
            if (Files.isRegularFile(modulePom)) {
                models.add(readModel(modulePom));
            }
        }
        return List.copyOf(models);
    }

    private static String detectJavaVersion(List<Model> models) {
        return firstConsistentValue(models.stream()
                .map(MavenProjectInspector::detectJavaVersion)
                .filter(value -> !"unknown".equals(value))
                .toList());
    }

    private static String detectJavaVersion(Model model) {
        var properties = model.getProperties();
        var declaredJavaVersion = firstNonBlank(
                properties,
                "java.version",
                "maven.compiler.release",
                "maven.compiler.source",
                "maven.compiler.target");
        if (declaredJavaVersion == null) {
            declaredJavaVersion = findCompilerPluginJavaVersion(model);
        }
        if (declaredJavaVersion == null) {
            return "unknown";
        }

        return normalizeJavaVersion(declaredJavaVersion);
    }

    private static String detectSpringBootVersion(List<Model> models) {
        return firstConsistentNullableValue(models.stream()
                .map(MavenProjectInspector::detectSpringBootVersion)
                .toList());
    }

    private static String findCompilerPluginJavaVersion(Model model) {
        var build = model.getBuild();
        if (build == null || build.getPlugins() == null) {
            return null;
        }

        for (Plugin plugin : build.getPlugins()) {
            if (!isCompilerPlugin(plugin)) {
                continue;
            }

            var configuration = plugin.getConfiguration();
            if (!(configuration instanceof Xpp3Dom dom)) {
                continue;
            }

            var declaredJavaVersion = firstNonBlank(dom, "release", "source", "target");
            if (declaredJavaVersion != null) {
                return declaredJavaVersion;
            }
        }

        return null;
    }

    private static String detectSpringBootVersion(Model model) {
        var parent = model.getParent();
        if (isSpringBootParent(parent)) {
            return normalizeOptionalVersion(parent.getVersion());
        }

        if (model.getDependencies() == null) {
            return null;
        }

        for (Dependency dependency : model.getDependencies()) {
            if (SPRING_BOOT_GROUP_ID.equals(dependency.getGroupId())) {
                var version = normalizeOptionalVersion(dependency.getVersion());
                if (version != null) {
                    return version;
                }
            }
        }

        return null;
    }

    private static boolean isSpringBootParent(Parent parent) {
        return parent != null && SPRING_BOOT_GROUP_ID.equals(parent.getGroupId());
    }

    private static List<String> collectDependencies(List<Model> models) {
        var dependencies = new LinkedHashSet<String>();
        for (Model model : models) {
            dependencies.addAll(collectDependencies(model));
        }
        return List.copyOf(dependencies);
    }

    private static List<String> collectDependencies(Model model) {
        if (model.getDependencies() == null) {
            return List.of();
        }

        var dependencies = new ArrayList<String>();
        for (Dependency dependency : model.getDependencies()) {
            if (dependency.getGroupId() != null && dependency.getArtifactId() != null) {
                dependencies.add(dependency.getGroupId() + ":" + dependency.getArtifactId());
            }
        }
        return List.copyOf(dependencies);
    }

    private static List<String> collectBuildPlugins(List<Model> models) {
        var plugins = new LinkedHashSet<String>();
        for (Model model : models) {
            plugins.addAll(collectBuildPlugins(model));
        }
        return List.copyOf(plugins);
    }

    private static List<String> collectBuildPlugins(Model model) {
        var build = model.getBuild();
        if (build == null || build.getPlugins() == null) {
            return List.of();
        }

        var plugins = new ArrayList<String>();
        for (Plugin plugin : build.getPlugins()) {
            if (plugin == null) {
                continue;
            }

            var artifactId = normalizeOptionalText(plugin.getArtifactId());
            if (artifactId == null) {
                continue;
            }

            var groupId = normalizeOptionalText(plugin.getGroupId());
            if (groupId == null) {
                groupId = "org.apache.maven.plugins";
            }
            plugins.add(groupId + ":" + artifactId);
        }
        return List.copyOf(plugins);
    }

    private static List<String> collectCompilerArgs(List<Model> models) {
        var compilerArgs = new LinkedHashSet<String>();
        for (Model model : models) {
            compilerArgs.addAll(collectCompilerArgs(model));
        }
        return List.copyOf(compilerArgs);
    }

    private static List<DependencyBaseline> collectDependencyBaselines(List<Model> models) {
        var baselines = new LinkedHashSet<DependencyBaseline>();
        for (Model model : models) {
            addPluginBaselines(model, baselines);
            addDependencyBaselines(model, baselines);
        }
        return List.copyOf(baselines);
    }

    private static void addPluginBaselines(Model model, Set<DependencyBaseline> baselines) {
        var build = model.getBuild();
        if (build == null || build.getPlugins() == null) {
            return;
        }

        for (Plugin plugin : build.getPlugins()) {
            var artifact = pluginCoordinate(plugin);
            var version = resolveProperty(model.getProperties(), normalizeOptionalVersion(plugin.getVersion()));
            if (artifact != null && version != null && isTrackedMavenPlugin(artifact)) {
                baselines.add(new DependencyBaseline("Build plugin", artifact, version, "pom.xml"));
            }
        }
    }

    private static void addDependencyBaselines(Model model, Set<DependencyBaseline> baselines) {
        if (model.getDependencies() == null) {
            return;
        }

        for (Dependency dependency : model.getDependencies()) {
            if (dependency.getGroupId() == null || dependency.getArtifactId() == null) {
                continue;
            }
            var artifact = dependency.getGroupId() + ":" + dependency.getArtifactId();
            var version = resolveProperty(model.getProperties(), normalizeOptionalVersion(dependency.getVersion()));
            if (version != null && isTrackedDependency(artifact)) {
                baselines.add(new DependencyBaseline(dependencyBaselineCategory(dependency), artifact, version, "pom.xml"));
            }
        }
    }

    private static String pluginCoordinate(Plugin plugin) {
        var artifactId = normalizeOptionalText(plugin.getArtifactId());
        if (artifactId == null) {
            return null;
        }
        var groupId = normalizeOptionalText(plugin.getGroupId());
        if (groupId == null) {
            groupId = "org.apache.maven.plugins";
        }
        return groupId + ":" + artifactId;
    }

    private static boolean isTrackedMavenPlugin(String artifact) {
        return "org.apache.maven.plugins:maven-surefire-plugin".equals(artifact)
                || "org.apache.maven.plugins:maven-failsafe-plugin".equals(artifact)
                || "org.apache.maven.plugins:maven-compiler-plugin".equals(artifact)
                || "org.springframework.boot:spring-boot-maven-plugin".equals(artifact);
    }

    private static boolean isTrackedDependency(String artifact) {
        return "org.projectlombok:lombok".equals(artifact)
                || "org.mockito:mockito-core".equals(artifact)
                || "org.mockito:mockito-inline".equals(artifact)
                || "net.bytebuddy:byte-buddy".equals(artifact);
    }

    private static String dependencyBaselineCategory(Dependency dependency) {
        var scope = normalizeOptionalText(dependency.getScope());
        if ("test".equals(scope)) {
            return "Test dependency";
        }
        if ("runtime".equals(scope)) {
            return "Runtime dependency";
        }
        return "Application dependency";
    }

    private static String resolveProperty(Properties properties, String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("${") && value.endsWith("}") && properties != null) {
            return normalizeOptionalVersion(properties.getProperty(value.substring(2, value.length() - 1)));
        }
        return value;
    }

    private static List<String> collectCompilerArgs(Model model) {
        var build = model.getBuild();
        if (build == null || build.getPlugins() == null) {
            return List.of();
        }

        Set<String> compilerArgs = new LinkedHashSet<>();
        for (Plugin plugin : build.getPlugins()) {
            if (!isCompilerPlugin(plugin) || !(plugin.getConfiguration() instanceof Xpp3Dom dom)) {
                if (isCompilerPlugin(plugin)) {
                    collectExecutionCompilerArgs(plugin, compilerArgs);
                }
                continue;
            }
            collectCompilerArgs(dom, compilerArgs);
            collectExecutionCompilerArgs(plugin, compilerArgs);
        }
        return List.copyOf(compilerArgs);
    }

    private static void collectExecutionCompilerArgs(Plugin plugin, Set<String> compilerArgs) {
        if (plugin.getExecutions() == null) {
            return;
        }

        for (var execution : plugin.getExecutions()) {
            if (execution.getConfiguration() instanceof Xpp3Dom dom) {
                collectCompilerArgs(dom, compilerArgs);
            }
        }
    }

    private static void collectCompilerArgs(Xpp3Dom configuration, Set<String> compilerArgs) {
        var compilerArgsNode = configuration.getChild("compilerArgs");
        if (compilerArgsNode != null) {
            for (Xpp3Dom child : compilerArgsNode.getChildren()) {
                addNonBlank(compilerArgs, child.getValue());
            }
        }

        var compilerArgumentNode = configuration.getChild("compilerArgument");
        if (compilerArgumentNode != null && compilerArgumentNode.getValue() != null) {
            for (String arg : compilerArgumentNode.getValue().trim().split("\\s+")) {
                addNonBlank(compilerArgs, arg);
            }
        }
    }

    private static String firstNonBlank(Properties properties, String... keys) {
        if (properties == null) {
            return null;
        }

        for (String key : keys) {
            var value = properties.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String firstNonBlank(Xpp3Dom dom, String... keys) {
        for (String key : keys) {
            var child = dom.getChild(key);
            if (child != null) {
                var value = child.getValue();
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static String firstConsistentValue(List<String> values) {
        var consistentValue = firstConsistentNullableValue(values);
        return consistentValue == null ? "unknown" : consistentValue;
    }

    private static String firstConsistentNullableValue(List<String> values) {
        String selected = null;
        for (String value : values) {
            var normalized = normalizeOptionalText(value);
            if (normalized == null) {
                continue;
            }
            if (selected == null) {
                selected = normalized;
            } else if (!selected.equals(normalized)) {
                return null;
            }
        }
        return selected;
    }

    private static boolean isCompilerPlugin(Plugin plugin) {
        return plugin != null && "maven-compiler-plugin".equals(plugin.getArtifactId());
    }

    private static String normalizeJavaVersion(String value) {
        var normalized = value.trim();
        if (normalized.startsWith("1.") && normalized.length() > 2) {
            return normalized.substring(2);
        }
        return normalized;
    }

    private static String normalizeOptionalVersion(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static void addNonBlank(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value.trim());
        }
    }
}
