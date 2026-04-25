package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkItemGeneratorTest {

    @Test
    void generatesEnterpriseWorkItemsFromRiskSignalsAndReadiness() {
        var metadata = new ProjectMetadata(
                "gradle",
                "11",
                "2.5.2",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new DependencyBaseline("Build tool", "Gradle wrapper", "6.8.3", "gradle/wrapper/gradle-wrapper.properties"),
                        new DependencyBaseline("Runtime image", "Jib base image", "openjdk:11.0.10-jre-buster", "build.gradle")),
                new BuildReadiness(true, "GitHub Actions", ".github/workflows/build.yml", "./gradlew test"),
                List.of());

        var result = new DefaultAnalyzer(metadata).analyze(new AnalysisRequest(Path.of("."), 21));

        assertThat(result.workItems())
                .extracting(WorkItem::id)
                .contains(
                        "run-baseline-tests",
                        "stage-spring-boot-2-7",
                        "upgrade-gradle-wrapper",
                        "replace-java-11-runtime-image",
                        "run-openrewrite-java-21");
        assertThat(result.workItems())
                .filteredOn(item -> item.id().equals("run-baseline-tests"))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.title()).isEqualTo("Run baseline tests in CI before migration changes");
                    assertThat(item.command()).isEqualTo("./gradlew test");
                });
    }
}
