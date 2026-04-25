package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuildReadinessRenderingTest {

    @Test
    void markdownRendersBuildReadinessAndWorkItems() {
        var metadata = new ProjectMetadata(
                "maven",
                "17",
                "3.3.5",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new BuildReadiness(true, "GitHub Actions", ".github/workflows/build.yml", "./mvnw test"),
                List.of());
        var result = new DefaultAnalyzer(metadata).analyze(new AnalysisRequest(Path.of("/workspace/app"), 21));

        var report = new MarkdownReportRenderer().render(new AnalysisRequest(Path.of("/workspace/app"), 21), result);

        assertThat(report).contains(
                """
                ## Build Readiness

                - Build wrapper present: Yes
                - CI provider: GitHub Actions
                - CI evidence: `.github/workflows/build.yml`
                - Suggested test command: `./mvnw test`
                """.stripTrailing());
        assertThat(report).contains(
                """
                ## Recommended Work Items

                ### [BUILD] Run baseline tests in CI before migration changes
                """.stripTrailing());
    }

    @Test
    void jsonRendersBuildReadinessAndWorkItems() {
        var metadata = new ProjectMetadata(
                "maven",
                "17",
                "3.3.5",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new BuildReadiness(true, "GitHub Actions", ".github/workflows/build.yml", "./mvnw test"),
                List.of());
        var request = new AnalysisRequest(Path.of("/workspace/app"), 21);
        var result = new DefaultAnalyzer(metadata).analyze(request);

        var json = new JsonReportRenderer().render(request, result);

        assertThat(json)
                .contains("\"buildReadiness\"")
                .contains("\"buildWrapperPresent\": true")
                .contains("\"ciProvider\": \"GitHub Actions\"")
                .contains("\"suggestedTestCommand\": \"./mvnw test\"")
                .contains("\"workItems\"");
    }
}
