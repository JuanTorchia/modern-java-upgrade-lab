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

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("# Modern Java Upgrade Report");
        assertThat(report).contains("Build tool: Maven");
        assertThat(report).contains("Declared Java version: 8");
        assertThat(report).contains("Target Java version: 21");
        assertThat(report).contains("Upgrade Spring Boot before moving to Java 21");
        assertThat(report).contains("OpenRewrite recipe");
    }

    @Test
    void rendersEmptyStateWhenNoFindingsExist() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata("maven", "8", "2.7.18", List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("No findings were generated yet.");
    }
}
