package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownReportRendererTest {

    @Test
    void rendersProjectSummaryAndFindings() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata(
                "maven",
                "8",
                "2.7.18",
                List.of("org.springframework.boot:spring-boot-starter-web"));
        var findings = List.of(new Finding(
                "spring-boot-upgrade",
                FindingCategory.FRAMEWORK,
                FindingSeverity.RISK,
                "Spring Boot",
                "Upgrade Spring Boot before moving to Java 21",
                "Spring Boot 2.7.18 is still on the older line.",
                "Upgrade to a Spring Boot 3.x baseline before adopting Java 21.",
                "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2"));
        var result = new AnalysisResult(metadata, 21, findings);
        var openRewriteCommand = "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run "
                + "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE "
                + "-Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2 "
                + "-Drewrite.exportDatatables=true";

        var expectedReport = """
                # Modern Java Upgrade Report

                Project path: `%s`

                ## Project Summary

                - Build tool: Maven
                - Declared Java version: 8
                - Target Java version: 21
                - Spring Boot version: 2.7.18

                ## Framework Compatibility

                ### [RISK] Upgrade Spring Boot before moving to Java 21

                - Area: Spring Boot
                - Evidence: Spring Boot 2.7.18 is still on the older line.
                - Recommendation: Upgrade to a Spring Boot 3.x baseline before adopting Java 21.
                - OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`
                - OpenRewrite command: `%s`
                """.formatted(request.projectPath().toAbsolutePath().normalize(), openRewriteCommand).stripTrailing();

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).isEqualTo(expectedReport);
    }

    @Test
    void rendersEmptyAnalysisResultWhenNoFindingsExist() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata("maven", "8", "2.7.18", List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("No findings were generated yet.");
        assertThat(report).contains("## Project Summary");
        assertThat(report).doesNotContain("## Findings");
    }

    @Test
    void omitsOpenRewriteRecipeLineWhenRecipeIsMissing() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata(
                "maven",
                "8",
                "2.7.18",
                List.of("org.springframework.boot:spring-boot-starter-web"));
        var findings = List.of(new Finding(
                "spring-boot-upgrade",
                FindingCategory.FRAMEWORK,
                FindingSeverity.RISK,
                "Spring Boot",
                "Upgrade Spring Boot before moving to Java 21",
                "Spring Boot 2.7.18 is still on the older line.",
                "Upgrade to a Spring Boot 3.x baseline before adopting Java 21.",
                null));
        var result = new AnalysisResult(metadata, 21, findings);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("Project path: `");
        assertThat(report).contains("## Framework Compatibility");
        assertThat(report).contains("### [RISK] Upgrade Spring Boot before moving to Java 21");
        assertThat(report).doesNotContain("- ID:");
        assertThat(report).doesNotContain("- OpenRewrite recipe:");
        assertThat(report).doesNotContain("null");
    }

    @Test
    void normalizesMarkdownSensitiveTextAndMissingOptionalMetadata() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata("maven", "8", null, List.of());
        var findings = List.of(new Finding(
                "spring-boot-upgrade",
                FindingCategory.FRAMEWORK,
                FindingSeverity.RISK,
                "Spring Boot",
                "Upgrade\nSpring Boot #3 now",
                "Line one\r\nLine two",
                "Use\nOpenRewrite",
                "  org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2  "));
        var result = new AnalysisResult(metadata, 21, findings);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("Spring Boot version: Unknown");
        assertThat(report).contains("Project path: `");
        assertThat(report).contains("## Project Summary");
        assertThat(report).contains("## Framework Compatibility");
        assertThat(report).contains("- Build tool: Maven");
        assertThat(report).contains("- Declared Java version: 8");
        assertThat(report).contains("- Target Java version: 21");
        assertThat(report).contains("- Spring Boot version: Unknown");
        assertThat(report).contains("### [RISK] Upgrade Spring Boot #3 now");
        assertThat(report).contains("- Evidence: Line one Line two");
        assertThat(report).contains("- Recommendation: Use OpenRewrite");
        assertThat(report).contains("- OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`");
        assertThat(report).contains("- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run");
        assertThat(report).doesNotContain("null");
    }

    @Test
    void rendersRepresentativeSectionSnapshots() {
        var request = new AnalysisRequest(Path.of("/workspace/java-21-project"), 25);
        var metadata = new ProjectMetadata(
                "gradle",
                "21",
                "3.3.5",
                List.of("org.springframework.boot:spring-boot-starter-web"));
        var findings = List.of(
                new Finding(
                        "jdk-25-preview-feature-boundary",
                        FindingCategory.BUILD,
                        FindingSeverity.RISK,
                        "Compiler flags",
                        "Preview feature usage is a Java 25 migration boundary",
                        "Detected compiler argument `--enable-preview`",
                        "Treat preview/incubator feature usage as explicit technical debt.",
                        null),
                new Finding(
                        "spring-boot-baseline",
                        FindingCategory.FRAMEWORK,
                        FindingSeverity.INFO,
                        "Spring Boot compatibility",
                        "Spring Boot baseline should be reviewed before a Java 25 rollout",
                        "Detected Spring Boot 3.3.5",
                        "Validate the selected Spring Boot line against Java 25 before runtime rollout.",
                        null),
                new Finding(
                        "source-map-string-object-controller",
                        FindingCategory.LANGUAGE,
                        FindingSeverity.INFO,
                        "Language modernization",
                        "Map-based response can be reviewed as an explicit DTO or record",
                        "src/main/java/example/Controller.java:12 contains `Map<String, Object>`",
                        "Review whether this loosely typed map represents a stable response shape.",
                        null),
                new Finding(
                        "source-thread-local-request-context",
                        FindingCategory.CONCURRENCY,
                        FindingSeverity.INFO,
                        "Concurrency modernization",
                        "ThreadLocal usage should be reviewed for scoped values",
                        "src/main/java/example/RequestContext.java:5 contains `ThreadLocal<String>`",
                        "Review whether this context propagation can move toward scoped values on Java 25.",
                        null),
                new Finding(
                        "openrewrite-java-25",
                        FindingCategory.AUTOMATION,
                        FindingSeverity.INFO,
                        "Migration automation",
                        "OpenRewrite has a Java 25 migration recipe",
                        "Target Java version is 25",
                        "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                        "org.openrewrite.java.migrate.UpgradeToJava25"),
                new Finding(
                        "java-21-to-25-baseline-review",
                        FindingCategory.BASELINE,
                        FindingSeverity.INFO,
                        "Java baseline",
                        "Java 21 to 25 migration should start with a build and test baseline",
                        "Declared Java version is 21; target Java version is 25",
                        "Establish a Java 25 build and test baseline before enabling optional changes.",
                        null));
        var result = new AnalysisResult(metadata, 25, findings);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).containsSubsequence(
                "## Build & Tooling",
                "## Framework Compatibility",
                "## Language Modernization",
                "## Concurrency",
                "## Automation Suggestions",
                "## Baseline & Planning");
        assertThat(report).contains(
                """
                ## Build & Tooling

                ### [RISK] Preview feature usage is a Java 25 migration boundary

                - Area: Compiler flags
                - Evidence: Detected compiler argument `--enable-preview`
                - Recommendation: Treat preview/incubator feature usage as explicit technical debt.
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Framework Compatibility

                ### [INFO] Spring Boot baseline should be reviewed before a Java 25 rollout

                - Area: Spring Boot compatibility
                - Evidence: Detected Spring Boot 3.3.5
                - Recommendation: Validate the selected Spring Boot line against Java 25 before runtime rollout.
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Language Modernization

                ### [INFO] Map-based response can be reviewed as an explicit DTO or record

                - Area: Language modernization
                - Evidence: src/main/java/example/Controller.java:12 contains `Map<String, Object>`
                - Recommendation: Review whether this loosely typed map represents a stable response shape.
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Concurrency

                ### [INFO] ThreadLocal usage should be reviewed for scoped values

                - Area: Concurrency modernization
                - Evidence: src/main/java/example/RequestContext.java:5 contains `ThreadLocal<String>`
                - Recommendation: Review whether this context propagation can move toward scoped values on Java 25.
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Automation Suggestions

                ### [INFO] OpenRewrite has a Java 25 migration recipe

                - Area: Migration automation
                - Evidence: Target Java version is 25
                - Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
                - OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Baseline & Planning

                ### [INFO] Java 21 to 25 migration should start with a build and test baseline

                - Area: Java baseline
                - Evidence: Declared Java version is 21; target Java version is 25
                - Recommendation: Establish a Java 25 build and test baseline before enabling optional changes.
                """.stripTrailing());
    }
}
