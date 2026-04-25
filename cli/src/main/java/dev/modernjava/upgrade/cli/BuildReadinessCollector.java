package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.core.BuildReadiness;
import java.nio.file.Files;
import java.nio.file.Path;

final class BuildReadinessCollector {

    BuildReadiness collect(Path projectPath, String buildTool) {
        var root = projectPath.toAbsolutePath().normalize();
        var wrapperPresent = hasWrapper(root, buildTool);
        var ci = detectCi(root);
        return new BuildReadiness(
                wrapperPresent,
                ci.provider(),
                ci.evidence(),
                BuildReadiness.defaultTestCommand(buildTool, wrapperPresent));
    }

    private static boolean hasWrapper(Path root, String buildTool) {
        if ("gradle".equalsIgnoreCase(buildTool)) {
            return Files.isRegularFile(root.resolve("gradlew"))
                    || Files.isRegularFile(root.resolve("gradlew.bat"));
        }
        if ("maven".equalsIgnoreCase(buildTool)) {
            return Files.isRegularFile(root.resolve("mvnw"))
                    || Files.isRegularFile(root.resolve("mvnw.cmd"));
        }
        return false;
    }

    private static CiSignal detectCi(Path root) {
        var githubWorkflows = root.resolve(".github").resolve("workflows");
        if (Files.isDirectory(githubWorkflows)) {
            try (var stream = Files.list(githubWorkflows)) {
                var workflow = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            var fileName = path.getFileName().toString().toLowerCase();
                            return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
                        })
                        .findFirst();
                if (workflow.isPresent()) {
                    return new CiSignal(
                            "GitHub Actions",
                            ".github/workflows/" + workflow.get().getFileName());
                }
            } catch (java.io.IOException ignored) {
                return new CiSignal("GitHub Actions", ".github/workflows");
            }
        }

        if (Files.isRegularFile(root.resolve("Jenkinsfile"))) {
            return new CiSignal("Jenkins", "Jenkinsfile");
        }
        if (Files.isRegularFile(root.resolve(".gitlab-ci.yml"))) {
            return new CiSignal("GitLab CI", ".gitlab-ci.yml");
        }
        if (Files.isRegularFile(root.resolve(".circleci").resolve("config.yml"))) {
            return new CiSignal("CircleCI", ".circleci/config.yml");
        }
        return new CiSignal(null, null);
    }

    private record CiSignal(String provider, String evidence) {
    }
}
