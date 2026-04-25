package dev.modernjava.upgrade.core;

import java.util.ArrayList;
import java.util.List;

final class WorkItemGenerator {

    List<WorkItem> generate(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {
        var items = new ArrayList<WorkItem>();
        addBaselineTestWorkItem(metadata, items);
        addSpringBootWorkItem(metadata, targetJavaVersion, items);
        addBaselineWorkItems(metadata, targetJavaVersion, items);
        addOpenRewriteWorkItems(targetJavaVersion, findings, items);
        return List.copyOf(items);
    }

    private static void addBaselineTestWorkItem(ProjectMetadata metadata, List<WorkItem> items) {
        items.add(new WorkItem(
                "run-baseline-tests",
                "BUILD",
                "Run baseline tests in CI before migration changes",
                "A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.",
                metadata.buildReadiness().suggestedTestCommand(),
                "P0",
                "Baseline"));
    }

    private static void addSpringBootWorkItem(ProjectMetadata metadata, int targetJavaVersion, List<WorkItem> items) {
        var springBootVersion = metadata.springBootVersion();
        if (targetJavaVersion >= 21
                && springBootVersion != null
                && springBootVersion.startsWith("2.")
                && !springBootVersion.startsWith("2.7.")) {
            items.add(new WorkItem(
                    "stage-spring-boot-2-7",
                    "FRAMEWORK",
                    "Stage Spring Boot 2.7.x before the Java " + targetJavaVersion + " rollout",
                    "Spring Boot " + springBootVersion
                            + " is below the safer Spring Boot 2.7.x staging baseline for Java "
                            + targetJavaVersion + " planning.",
                    null,
                    "P1",
                    "Framework"));
        }
    }

    private static void addBaselineWorkItems(ProjectMetadata metadata, int targetJavaVersion, List<WorkItem> items) {
        for (DependencyBaseline baseline : metadata.dependencyBaselines()) {
            if ("Gradle wrapper".equals(baseline.name()) && targetJavaVersion >= 21) {
                items.add(new WorkItem(
                        "upgrade-gradle-wrapper",
                        "BUILD",
                        "Validate or upgrade the Gradle wrapper before Java " + targetJavaVersion,
                        "Detected Gradle wrapper " + baseline.version()
                                + ". Older Gradle versions can block newer Java toolchains.",
                        "./gradlew wrapper --gradle-version <validated-version>",
                        "P1",
                        "Build"));
            }
            if ("Runtime image".equals(baseline.category())
                    && baseline.version() != null
                    && baseline.version().toLowerCase().contains("11")
                    && targetJavaVersion >= 21) {
                items.add(new WorkItem(
                        "replace-java-11-runtime-image",
                        "RUNTIME",
                        "Replace Java 11 runtime image before Java " + targetJavaVersion + " rollout",
                        "Detected runtime image " + baseline.version()
                                + ", which does not match the requested Java " + targetJavaVersion + " runtime.",
                        null,
                        "P1",
                        "Runtime"));
            }
        }
    }

    private static void addOpenRewriteWorkItems(int targetJavaVersion, List<Finding> findings, List<WorkItem> items) {
        findings.stream()
                .filter(finding -> finding.openRewriteRecipe() != null && !finding.openRewriteRecipe().isBlank())
                .findFirst()
                .ifPresent(finding -> items.add(new WorkItem(
                        "run-openrewrite-java-" + targetJavaVersion,
                        "AUTOMATION",
                        "Run OpenRewrite Java " + targetJavaVersion + " recipe in a branch",
                        "OpenRewrite automation should be reviewable and separate from manual runtime changes.",
                        "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes="
                                + finding.openRewriteRecipe(),
                        "P2",
                        "Automation")));
    }
}
