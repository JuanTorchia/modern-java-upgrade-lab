package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.ProjectMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ProjectInspector {

    public ProjectMetadata inspect(Path projectPath) {
        Objects.requireNonNull(projectPath, "projectPath");
        if (isPomFile(projectPath) || Files.isRegularFile(projectPath.resolve("pom.xml"))) {
            return new MavenProjectInspector().inspect(projectPath);
        }
        if (isGradleBuildFile(projectPath)
                || Files.isRegularFile(projectPath.resolve("build.gradle.kts"))
                || Files.isRegularFile(projectPath.resolve("build.gradle"))) {
            return new GradleProjectInspector().inspect(projectPath);
        }

        throw new IllegalArgumentException(
                "No Maven or Gradle build file found at " + projectPath.toAbsolutePath().normalize());
    }

    private static boolean isPomFile(Path path) {
        return Files.isRegularFile(path) && "pom.xml".equals(path.getFileName().toString());
    }

    private static boolean isGradleBuildFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        var fileName = path.getFileName().toString();
        return "build.gradle".equals(fileName) || "build.gradle.kts".equals(fileName);
    }
}
