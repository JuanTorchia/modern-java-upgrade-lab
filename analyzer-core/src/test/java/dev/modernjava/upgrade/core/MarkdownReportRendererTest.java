package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
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
                - Migration status: Upgrade required (Java 8 -> 21)
                - Spring Boot version: 2.7.18

                ## Risk Assessment

                - Risk level: HIGH
                - Risk score: 65/100
                - Reason: Declared Java 8 targets Java 21
                - Reason: Java 8 to Java 21 crosses multiple LTS baselines
                - Reason: Report contains 1 risk-severity finding(s)

                ## Build Readiness

                - Build wrapper present: No
                - CI provider: Unknown
                - CI evidence: `Unknown`
                - Suggested test command: `mvn test`

                ## Analysis Metadata

                - Analyzer version: unknown
                - Generated at: Unknown
                - Source path: `%s`
                - Git commit: `Unknown`
                - Git branch: `Unknown`
                - Target Java: 21

                ## Recommended Work Items

                ### [BUILD] Run baseline tests in CI before migration changes

                - Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
                - Command: `mvn test`

                ### [AUTOMATION] Run OpenRewrite Java 21 recipe in a branch

                - Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
                - Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`

                ## Migration Plan

                ### Phase 1: Baseline

                - Confirm the current Java version in local development and CI.
                - Run the full test suite before changing the Java target.
                - Make compiler, toolchain, and runtime configuration explicit.

                ### Phase 2: Framework Compatibility

                - Validate Spring Boot 2.7.18 support before moving to Java 21.
                - Move to Spring Boot 2.7.x first when staying on the Spring Boot 2 line.
                - Treat Spring Boot 3.x as a separate migration because it introduces Jakarta namespace changes.

                ### Phase 3: Automated Changes

                - Run suggested OpenRewrite recipes in a dedicated branch.
                - Review generated diffs and datatables before merging.

                ### Phase 4: Manual Review

                - Review source modernization candidates after the migration baseline is stable.
                - Keep optional refactors out of the baseline migration branch.

                ### Phase 5: Rollout

                - Validate CI, container images, runtime flags, observability, and rollback paths.
                - Roll out the runtime upgrade separately from broad application refactors.

                ## Framework Compatibility

                ### [RISK] Upgrade Spring Boot before moving to Java 21

                - Area: Spring Boot
                - Evidence: Spring Boot 2.7.18 is still on the older line.
                - Recommendation: Upgrade to a Spring Boot 3.x baseline before adopting Java 21.
                - OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`
                - OpenRewrite command: `%s`
                """.formatted(
                        request.projectPath().toAbsolutePath().normalize(),
                        request.projectPath().toAbsolutePath().normalize(),
                        openRewriteCommand).stripTrailing();

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report)
                .contains("# Modern Java Upgrade Report")
                .contains("## Executive Summary")
                .contains("## Migration Blockers")
                .contains("## Recommended Work Items")
                .contains("- Priority: P0")
                .contains("- Phase: Baseline")
                .contains("## Suggested Commands")
                .contains("## Framework Compatibility")
                .contains("- OpenRewrite command: `%s`".formatted(openRewriteCommand));
    }

    @Test
    void rendersAnalysisMetadataWhenAvailable() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata("maven", "17", "3.3.5", List.of());
        var analysisMetadata = new AnalysisMetadata(
                "0.1.0-SNAPSHOT",
                Instant.parse("2026-04-23T22:00:00Z"),
                "abc123def456",
                "main");
        var result = new AnalysisResult(metadata, 21, List.of(), analysisMetadata);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains(
                """
                ## Analysis Metadata

                - Analyzer version: 0.1.0-SNAPSHOT
                - Generated at: 2026-04-23T22:00:00Z
                - Source path: `%s`
                - Git commit: `abc123def456`
                - Git branch: `main`
                - Target Java: 21
                """.formatted(request.projectPath().toAbsolutePath().normalize()).stripTrailing());
    }

    @Test
    void rendersEmptyAnalysisResultWhenNoFindingsExist() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata("maven", "8", "2.7.18", List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("## Migration Plan");
        assertThat(report).contains("No findings were generated yet.");
        assertThat(report).contains("## Project Summary");
        assertThat(report).doesNotContain("## Findings");
    }

    @Test
    void rendersAlreadyAtTargetMigrationStatus() {
        var request = new AnalysisRequest(Path.of("/workspace/current-project"), 21);
        var metadata = new ProjectMetadata("maven", "21", "4.0.3", List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("- Migration status: Already at target Java 21");
    }

    @Test
    void rendersRiskAssessmentSummary() {
        var request = new AnalysisRequest(Path.of("/workspace/risky-project"), 21);
        var metadata = new ProjectMetadata("gradle", "11", "2.5.2", List.of(), List.of());
        var result = new DefaultAnalyzer(metadata).analyze(request);

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains(
                """
                ## Risk Assessment

                - Risk level: HIGH
                - Risk score: """
        );
        assertThat(report).contains("- Reason: Declared Java 11 targets Java 21");
    }

    @Test
    void rendersUnknownBaselineMigrationStatus() {
        var request = new AnalysisRequest(Path.of("/workspace/unknown-project"), 21);
        var metadata = new ProjectMetadata("maven", "unknown", null, List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("- Migration status: Baseline unknown; verify Java version before planning Java 21");
    }

    @Test
    void rendersTargetBelowBaselineMigrationStatus() {
        var request = new AnalysisRequest(Path.of("/workspace/newer-project"), 17);
        var metadata = new ProjectMetadata("maven", "21", "3.3.5", List.of());
        var result = new AnalysisResult(metadata, 17, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains("- Migration status: Target Java 17 is below declared Java 21");
    }

    @Test
    void rendersMigrationPlanWithFrameworkGuidance() {
        var request = new AnalysisRequest(Path.of("/workspace/spring-boot-2-project"), 21);
        var metadata = new ProjectMetadata("gradle", "11", "2.6.3", List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).containsSubsequence(
                "## Project Summary",
                "## Migration Plan",
                "### Phase 1: Baseline",
                "### Phase 2: Framework Compatibility",
                "### Phase 3: Automated Changes",
                "### Phase 4: Manual Review",
                "### Phase 5: Rollout",
                "No findings were generated yet.");
        assertThat(report).contains(
                """
                ### Phase 2: Framework Compatibility

                - Validate Spring Boot 2.6.3 support before moving to Java 21.
                - Move to Spring Boot 2.7.x first when staying on the Spring Boot 2 line.
                - Treat Spring Boot 3.x as a separate migration because it introduces Jakarta namespace changes.
                """.stripTrailing());
    }

    @Test
    void rendersInspectionDiagnosticsInDedicatedSection() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 25);
        var metadata = new ProjectMetadata(
                "gradle",
                "21",
                "3.3.5",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new InspectorDiagnostic(
                        "Gradle version catalog",
                        InspectorDiagnosticSeverity.WARNING,
                        "Could not parse version catalog; catalog aliases were skipped.",
                        Path.of("gradle", "libs.versions.toml"))));
        var result = new AnalysisResult(metadata, 25, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains(
                """
                ## Inspection Diagnostics

                ### [WARNING] Gradle version catalog

                - Message: Could not parse version catalog; catalog aliases were skipped.
                - Path: `%s`
                """.formatted(Path.of("gradle", "libs.versions.toml")).stripTrailing());
        assertThat(report).contains("No findings were generated yet.");
    }

    @Test
    void rendersDependencyAndPluginBaselinesInDedicatedSection() {
        var request = new AnalysisRequest(Path.of("/workspace/sample-project"), 21);
        var metadata = new ProjectMetadata(
                "gradle",
                "11",
                "2.5.2",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new DependencyBaseline(
                        "Build plugin",
                        "org.springframework.boot",
                        "2.5.2",
                        "build.gradle")),
                List.of());
        var result = new AnalysisResult(metadata, 21, List.of());

        var report = new MarkdownReportRenderer().render(request, result);

        assertThat(report).contains(
                """
                ## Dependency & Plugin Baselines

                | Category | Name | Version | Evidence |
                | --- | --- | --- | --- |
                | Build plugin | org.springframework.boot | 2.5.2 | `build.gradle` |
                """.stripTrailing());
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
