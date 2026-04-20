package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.ProjectMetadata;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public final class MavenProjectInspector {

    private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";

    public ProjectMetadata inspect(Path projectPath) {
        var pomPath = resolvePomPath(Objects.requireNonNull(projectPath, "projectPath"));
        var model = readModel(pomPath);

        return new ProjectMetadata(
                "maven",
                detectJavaVersion(model),
                detectSpringBootVersion(model),
                collectDependencies(model));
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

    private static String detectJavaVersion(Model model) {
        var properties = model.getProperties();
        var declaredJavaVersion = firstNonBlank(
                properties,
                "java.version",
                "maven.compiler.release",
                "maven.compiler.target");
        if (declaredJavaVersion == null) {
            return "unknown";
        }

        return normalizeJavaVersion(declaredJavaVersion);
    }

    private static String detectSpringBootVersion(Model model) {
        var parent = model.getParent();
        if (isSpringBootParent(parent)) {
            return normalizeOptionalVersion(parent.getVersion());
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

    private static List<String> collectDependencies(Model model) {
        var dependencies = new ArrayList<String>();
        for (Dependency dependency : model.getDependencies()) {
            if (dependency.getGroupId() != null && dependency.getArtifactId() != null) {
                dependencies.add(dependency.getGroupId() + ":" + dependency.getArtifactId());
            }
        }
        return List.copyOf(dependencies);
    }

    private static String firstNonBlank(Properties properties, String... keys) {
        for (String key : keys) {
            var value = properties.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
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
}
