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

                ## Summary

                - Build tool: Maven
                - Declared Java version: 8
                - Target Java version: 21
                - Spring Boot version: 2.7.18

                ## Findings

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
                FindingSeverity.RISK,
                "Spring Boot",
                "Upgrade Spring Boot before moving to Java 21",
                "Spring Boot 2.7.18 is still on the older line.",
                "Upgrade to a Spring Boot 3.x baseline before adopting Java 21.",
                null));
        var result = new AnalysisResult(metadata, 21, findings);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("Project path: `");
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
}
