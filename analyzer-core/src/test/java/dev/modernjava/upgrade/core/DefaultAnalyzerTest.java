package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultAnalyzerTest {

    @Test
    void reportsJava8To17MigrationFindingsForMavenSpringBootProject() {
        var metadata = new ProjectMetadata(
                "maven",
                "8",
                "2.7.18",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of());
        var request = new AnalysisRequest(Path.of("."), 17);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.targetJavaVersion()).isEqualTo(17);
        assertThat(result.metadata()).isEqualTo(metadata);
        assertThat(result.findings())
                .extracting(Finding::id)
                .containsExactly(
                        "java-8-baseline-target-17",
                        "spring-boot-2-java-17-risk",
                        "maven-compiler-plugin-explicit-config",
                        "openrewrite-java-17");
        assertThat(result.findings())
                .extracting(Finding::category)
                .containsExactly(
                        FindingCategory.BASELINE,
                        FindingCategory.FRAMEWORK,
                        FindingCategory.BUILD,
                        FindingCategory.AUTOMATION);
        assertThat(result.findings())
                .extracting(Finding::title)
                .contains(
                        "Java 8 baseline should be migrated deliberately before adopting Java 17",
                        "Spring Boot 2.x needs compatibility validation before a Java 17 migration",
                        "Maven compiler configuration should be explicit for Java 17 migration evidence",
                        "OpenRewrite has a Java 17 migration recipe");
    }

    @Test
    void doesNotReportJava8To17FindingsWhenTargetIsBelow17() {
        var metadata = new ProjectMetadata("maven", "8", "2.7.18", List.of(), List.of());
        var request = new AnalysisRequest(Path.of("."), 11);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings()).isEmpty();
    }

    @Test
    void keepsJava21MigrationAutomationFinding() {
        var metadata = new ProjectMetadata(
                "maven",
                "8",
                "2.7.18",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of("org.apache.maven.plugins:maven-compiler-plugin"));
        var request = new AnalysisRequest(Path.of("."), 21);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .extracting(Finding::id)
                .contains("openrewrite-java-21");
        assertThat(result.findings())
                .extracting(Finding::title)
                .contains("OpenRewrite has a Java 21 migration recipe");
    }
}
