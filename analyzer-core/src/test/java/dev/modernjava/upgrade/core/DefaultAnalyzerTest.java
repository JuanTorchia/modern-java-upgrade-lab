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

    @Test
    void reportsSpringBootBaselineReviewForJava17To21Migration() {
        var metadata = new ProjectMetadata(
                "maven",
                "17",
                "3.1.12",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of("org.apache.maven.plugins:maven-compiler-plugin"));
        var request = new AnalysisRequest(Path.of("."), 21);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .extracting(Finding::id)
                .contains("spring-boot-java-17-to-21-baseline-review");
        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("spring-boot-java-17-to-21-baseline-review"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.FRAMEWORK);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.INFO);
                    assertThat(finding.evidence()).isEqualTo("Declared Java version is 17; detected Spring Boot 3.1.12; target Java version is 21");
                    assertThat(finding.recommendation())
                            .contains("Validate the selected Spring Boot line against Java 21")
                            .contains("before runtime rollout");
                });
    }

    @Test
    void doesNotReportSpringBootBaselineReviewOutsideJava17To21Migration() {
        var metadata = new ProjectMetadata(
                "maven",
                "17",
                "3.1.12",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of("org.apache.maven.plugins:maven-compiler-plugin"));
        var request = new AnalysisRequest(Path.of("."), 17);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .extracting(Finding::id)
                .doesNotContain("spring-boot-java-17-to-21-baseline-review");
    }

    @Test
    void reportsJava21To25BaselineReview() {
        var metadata = new ProjectMetadata(
                "gradle",
                "21",
                "3.4.4",
                List.of("org.springframework.boot:spring-boot-starter-web"),
                List.of("org.springframework.boot"));
        var request = new AnalysisRequest(Path.of("."), 25);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("java-21-to-25-baseline-review"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.BASELINE);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.INFO);
                    assertThat(finding.evidence()).isEqualTo("Declared Java version is 21; target Java version is 25");
                    assertThat(finding.recommendation())
                            .contains("Establish a Java 25 build and test baseline")
                            .contains("language")
                            .contains("runtime")
                            .contains("GC")
                            .contains("JFR")
                            .contains("AOT");
                });
    }

    @Test
    void doesNotReportJava21To25BaselineReviewForOtherTransitions() {
        var java17Metadata = new ProjectMetadata("maven", "17", "3.1.12", List.of(), List.of());
        var java21Metadata = new ProjectMetadata("maven", "21", "3.4.4", List.of(), List.of());

        var java17To25 = new DefaultAnalyzer(java17Metadata).analyze(new AnalysisRequest(Path.of("."), 25));
        var java21To21 = new DefaultAnalyzer(java21Metadata).analyze(new AnalysisRequest(Path.of("."), 21));

        assertThat(java17To25.findings())
                .extracting(Finding::id)
                .doesNotContain("java-21-to-25-baseline-review");
        assertThat(java21To21.findings())
                .extracting(Finding::id)
                .doesNotContain("java-21-to-25-baseline-review");
    }

    @Test
    void reportsThreadLocalAsScopedValuesReviewForJava21To25Migration() {
        var metadata = new ProjectMetadata(
                "maven",
                "21",
                "3.4.4",
                List.of(),
                List.of(),
                List.of(new SourcePattern(
                        SourcePatternType.THREAD_LOCAL,
                        Path.of("src/main/java/example/RequestContext.java"),
                        6,
                        "private static final ThreadLocal<String> TENANT = new ThreadLocal<>();")));
        var request = new AnalysisRequest(Path.of("."), 25);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("source-thread-local-src-main-java-example-requestcontext-java-6"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.CONCURRENCY);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.INFO);
                    assertThat(finding.title()).isEqualTo("ThreadLocal usage should be reviewed for scoped values");
                    assertThat(finding.recommendation())
                            .contains("scoped values on Java 25")
                            .contains("Do not rewrite automatically");
                });
    }

    @Test
    void doesNotReportThreadLocalAsScopedValuesReviewOutsideJava21To25Migration() {
        var sourcePatterns = List.of(new SourcePattern(
                SourcePatternType.THREAD_LOCAL,
                Path.of("src/main/java/example/RequestContext.java"),
                6,
                "private static final ThreadLocal<String> TENANT = new ThreadLocal<>();"));
        var java17Metadata = new ProjectMetadata("maven", "17", "3.1.12", List.of(), List.of(), sourcePatterns);
        var java21Metadata = new ProjectMetadata("maven", "21", "3.4.4", List.of(), List.of(), sourcePatterns);

        var java17To25 = new DefaultAnalyzer(java17Metadata).analyze(new AnalysisRequest(Path.of("."), 25));
        var java21To21 = new DefaultAnalyzer(java21Metadata).analyze(new AnalysisRequest(Path.of("."), 21));

        assertThat(java17To25.findings())
                .extracting(Finding::id)
                .doesNotContain("source-thread-local-src-main-java-example-requestcontext-java-6");
        assertThat(java21To21.findings())
                .extracting(Finding::id)
                .doesNotContain("source-thread-local-src-main-java-example-requestcontext-java-6");
    }

    @Test
    void reportsPreviewCompilerArgsAsJava25Boundary() {
        var metadata = new ProjectMetadata(
                "gradle",
                "21",
                "3.4.4",
                List.of(),
                List.of(),
                List.of("--enable-preview"),
                List.of());
        var request = new AnalysisRequest(Path.of("."), 25);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("jdk-25-preview-feature-boundary"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.BUILD);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.RISK);
                    assertThat(finding.evidence()).contains("--enable-preview");
                    assertThat(finding.recommendation())
                            .contains("explicit technical debt")
                            .contains("every JDK update");
                });
    }

    @Test
    void doesNotReportPreviewBoundaryOutsideJava21To25Migration() {
        var metadata = new ProjectMetadata(
                "gradle",
                "21",
                "3.4.4",
                List.of(),
                List.of(),
                List.of("--enable-preview"),
                List.of());

        var result = new DefaultAnalyzer(metadata).analyze(new AnalysisRequest(Path.of("."), 21));

        assertThat(result.findings())
                .extracting(Finding::id)
                .doesNotContain("jdk-25-preview-feature-boundary");
    }

    @Test
    void reportsUnsafeUsageForJava21To25Migration() {
        var metadata = new ProjectMetadata(
                "maven",
                "21",
                "3.4.4",
                List.of(),
                List.of(),
                List.of(new SourcePattern(
                        SourcePatternType.UNSAFE_MEMORY_ACCESS,
                        Path.of("src/main/java/example/UnsafeHolder.java"),
                        6,
                        "private static final Unsafe UNSAFE = lookupUnsafe();")));
        var request = new AnalysisRequest(Path.of("."), 25);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("source-unsafe-memory-access-src-main-java-example-unsafeholder-java-6"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.BUILD);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.RISK);
                    assertThat(finding.recommendation())
                            .contains("Remove direct unsafe memory-access usage")
                            .contains("audit dependencies");
                });
    }

    @Test
    void reportsStructuredConcurrencyPreviewAsJava25Boundary() {
        var metadata = new ProjectMetadata(
                "maven",
                "21",
                "3.4.4",
                List.of(),
                List.of(),
                List.of(new SourcePattern(
                        SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW,
                        Path.of("src/main/java/example/StructuredWorker.java"),
                        3,
                        "import java.util.concurrent.StructuredTaskScope;")));
        var request = new AnalysisRequest(Path.of("."), 25);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .filteredOn(finding -> finding.id().equals("source-structured-concurrency-preview-src-main-java-example-structuredworker-java-3"))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.category()).isEqualTo(FindingCategory.CONCURRENCY);
                    assertThat(finding.severity()).isEqualTo(FindingSeverity.RISK);
                    assertThat(finding.title()).isEqualTo("Structured concurrency preview usage is a Java 25 migration boundary");
                    assertThat(finding.recommendation())
                            .contains("Keep structured concurrency behind explicit preview boundaries")
                            .contains("Do not present it as stable migration work");
                });
    }

    @Test
    void doesNotReportStructuredConcurrencyPreviewOutsideJava21To25Migration() {
        var sourcePatterns = List.of(new SourcePattern(
                SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW,
                Path.of("src/main/java/example/StructuredWorker.java"),
                3,
                "import java.util.concurrent.StructuredTaskScope;"));
        var java17Metadata = new ProjectMetadata("maven", "17", "3.1.12", List.of(), List.of(), sourcePatterns);
        var java21Metadata = new ProjectMetadata("maven", "21", "3.4.4", List.of(), List.of(), sourcePatterns);

        var java17To25 = new DefaultAnalyzer(java17Metadata).analyze(new AnalysisRequest(Path.of("."), 25));
        var java21To21 = new DefaultAnalyzer(java21Metadata).analyze(new AnalysisRequest(Path.of("."), 21));

        assertThat(java17To25.findings())
                .extracting(Finding::id)
                .doesNotContain("source-structured-concurrency-preview-src-main-java-example-structuredworker-java-3");
        assertThat(java21To21.findings())
                .extracting(Finding::id)
                .doesNotContain("source-structured-concurrency-preview-src-main-java-example-structuredworker-java-3");
    }

    @Test
    void reportsSourceModernizationFindings() {
        var metadata = new ProjectMetadata(
                "maven",
                "8",
                "2.7.18",
                List.of(),
                List.of(),
                List.of(
                        new SourcePattern(
                                SourcePatternType.MAP_STRING_OBJECT,
                                Path.of("src/main/java/example/LegacyController.java"),
                                8,
                                "Map<String, Object> response() {"),
                        new SourcePattern(
                                SourcePatternType.EXECUTOR_FACTORY,
                                Path.of("src/main/java/example/LegacyWorker.java"),
                                12,
                                "Executors.newFixedThreadPool(4)")));
        var request = new AnalysisRequest(Path.of("."), 21);

        var result = new DefaultAnalyzer(metadata).analyze(request);

        assertThat(result.findings())
                .extracting(Finding::id)
                .contains(
                        "source-map-string-object-src-main-java-example-legacycontroller-java-8",
                        "source-executor-factory-src-main-java-example-legacyworker-java-12");
        assertThat(result.findings())
                .extracting(Finding::category)
                .contains(FindingCategory.LANGUAGE, FindingCategory.CONCURRENCY);
        assertThat(result.findings())
                .extracting(Finding::title)
                .contains(
                        "Map-based response can be reviewed as an explicit DTO or record",
                        "Executor factory usage should be reviewed before adopting virtual threads");
    }
}
