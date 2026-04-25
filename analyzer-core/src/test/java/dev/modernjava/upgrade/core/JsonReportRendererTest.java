package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonReportRendererTest {

    @Test
    void rendersMachineReadableRiskSummaryAndFindings() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata(
                "gradle",
                "11",
                "2.5.2",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of("org.springframework.boot"),
                List.of(),
                List.of(),
                List.of(new DependencyBaseline(
                        "Build plugin",
                        "org.springframework.boot",
                        "2.5.2",
                        "build.gradle")),
                List.of());
        var result = new DefaultAnalyzer(metadata).analyze(request);

        var json = new JsonReportRenderer().render(request, result);

        assertThat(json)
                .contains("\"targetJavaVersion\": 21")
                .contains("\"riskLevel\": \"HIGH\"")
                .contains("\"riskScore\":")
                .contains("\"declaredJavaVersion\": \"11\"")
                .contains("\"springBootVersion\": \"2.5.2\"")
                .contains("\"dependencyBaselines\"")
                .contains("\"priority\": \"P0\"")
                .contains("\"phase\": \"Baseline\"")
                .contains("\"blockerCategory\": \"FRAMEWORK_BASELINE\"")
                .contains("\"findings\"")
                .contains("\"id\": \"spring-boot-2-java-21-risk\"");
    }
}
