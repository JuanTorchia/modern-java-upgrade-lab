package dev.modernjava.upgrade.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

class AnalyzeCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void rootCommandHelpIncludesAnalyzeSubcommand() {
        StringWriter output = new StringWriter();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output));

        int exitCode = commandLine.execute("--help");

        assertThat(exitCode).isZero();
        assertThat(output.toString()).contains("analyze");
    }

    @Test
    void analyzeCommandRendersJava8To17RuleReport() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-2-java-8",
                "--target",
                "17");

        assertThat(exitCode).isZero();
        String text = output.toString(StandardCharsets.UTF_8);
        assertThat(text).contains("# Modern Java Upgrade Report");
        assertThat(text).contains("Declared Java version: 8");
        assertThat(text).contains("Spring Boot version: 2.7.18");
        assertThat(text).contains("Java 8 baseline should be migrated deliberately before adopting Java 17");
        assertThat(text).contains("Spring Boot 2.x needs compatibility validation before a Java 17 migration");
        assertThat(text).contains("OpenRewrite has a Java 17 migration recipe");
        assertThat(text).contains("## Language Modernization");
        assertThat(text).contains("Map-based response can be reviewed as an explicit DTO or record");
    }

    @Test
    void analyzeCommandRendersGradleProjectReport() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-3-gradle-java-21",
                "--target",
                "25");

        assertThat(exitCode).isZero();
        String text = output.toString(StandardCharsets.UTF_8);
        assertThat(text).contains("# Modern Java Upgrade Report");
        assertThat(text).contains("Build tool: Gradle");
        assertThat(text).contains("Declared Java version: 21");
        assertThat(text).contains("Spring Boot version: 3.3.5");
        assertThat(text).contains("OpenRewrite has a Java 25 migration recipe");
        assertThat(text).contains("## Language Modernization");
        assertThat(text).contains("Map-based response can be reviewed as an explicit DTO or record");
    }

    @Test
    void analyzeCommandPreservesCompilerArgsWhenRenderingPreviewBoundary() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        var buildFile = tempDir.resolve("build.gradle.kts");
        Files.writeString(buildFile, """
                plugins {
                    java
                }

                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(21)
                    }
                }

                tasks.withType<JavaCompile>().configureEach {
                    options.compilerArgs.add("--enable-preview")
                }
                """);

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                tempDir.toString(),
                "--target",
                "25");

        assertThat(exitCode).isZero();
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Preview feature usage is a Java 25 migration boundary")
                .contains("--enable-preview");
    }

    @Test
    void analyzeCommandWritesReportToOutputFile() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        var reportPath = tempDir.resolve("reports").resolve("java-21-report.md");

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-3-gradle-java-21",
                "--target",
                "25",
                "--output",
                reportPath.toString());

        assertThat(exitCode).isZero();
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Report written to")
                .doesNotContain("# Modern Java Upgrade Report");
        assertThat(Files.readString(reportPath))
                .contains("# Modern Java Upgrade Report")
                .contains("## Analysis Metadata")
                .contains("Analyzer version:")
                .contains("Generated at:")
                .contains("Git commit:")
                .contains("Git branch:")
                .contains("Build tool: Gradle")
                .contains("Target Java version: 25");
    }

    @Test
    void analyzeCommandRendersJsonWhenRequested() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-2-java-8",
                "--target",
                "21",
                "--format",
                "json");

        assertThat(exitCode).isZero();
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("\"targetJavaVersion\": 21")
                .contains("\"riskLevel\": \"HIGH\"")
                .contains("\"findings\"")
                .doesNotContain("# Modern Java Upgrade Report");
    }

    @Test
    void analyzeCommandFailsWhenRiskMeetsConfiguredThreshold() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        commandLine.setErr(new PrintWriter(error, true));

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-2-java-8",
                "--target",
                "21",
                "--format",
                "json",
                "--fail-on-risk",
                "HIGH");

        assertThat(exitCode).isEqualTo(2);
        assertThat(error.toString(StandardCharsets.UTF_8)).contains("Risk threshold failed: HIGH >= HIGH");
    }

    @Test
    void analyzeCommandPassesWhenRiskIsBelowConfiguredThreshold() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        commandLine.setErr(new PrintWriter(error, true));

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                "../examples/spring-boot-2-java-8",
                "--target",
                "11",
                "--format",
                "json",
                "--fail-on-risk",
                "HIGH");

        assertThat(exitCode).isZero();
        assertThat(error.toString(StandardCharsets.UTF_8)).doesNotContain("Risk threshold failed");
    }

    @Test
    void analyzeCommandJsonIncludesBuildReadinessSignals() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        Files.writeString(tempDir.resolve("pom.xml"), """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>example</groupId>
                  <artifactId>sample</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <java.version>17</java.version>
                    <spring-boot.version>3.3.5</spring-boot.version>
                  </properties>
                </project>
                """);
        Files.writeString(tempDir.resolve("mvnw"), "maven wrapper placeholder");
        var workflowDir = tempDir.resolve(".github").resolve("workflows");
        Files.createDirectories(workflowDir);
        Files.writeString(workflowDir.resolve("build.yml"), "name: build\n");

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                tempDir.toString(),
                "--target",
                "21",
                "--format",
                "json");

        assertThat(exitCode).isZero();
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("\"buildWrapperPresent\": true")
                .contains("\"ciProvider\": \"GitHub Actions\"")
                .contains("\"ciEvidence\": \".github/workflows/build.yml\"")
                .contains("\"suggestedTestCommand\": \"./mvnw test\"")
                .contains("\"workItems\"")
                .contains("\"priority\": \"P0\"")
                .contains("\"phase\": \"Baseline\"");
    }

    @Test
    void portfolioCommandGeneratesSummaryFromJsonReports() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output, true));
        var input = tempDir.resolve("reports");
        Files.createDirectories(input);
        Files.writeString(input.resolve("app-a.json"), """
                {
                  "targetJavaVersion": 21,
                  "riskLevel": "HIGH",
                  "riskScore": 90,
                  "project": {
                    "buildTool": "gradle",
                    "declaredJavaVersion": "11",
                    "springBootVersion": "2.5.2"
                  },
                  "findings": [
                    {"id": "spring-boot-2-java-21-risk", "blockerCategory": "FRAMEWORK_BASELINE", "severity": "RISK"},
                    {"id": "openrewrite-java-21", "blockerCategory": "AUTOMATION", "severity": "INFO"}
                  ]
                }
                """);
        Files.writeString(input.resolve("app-b.json"), """
                {
                  "targetJavaVersion": 17,
                  "riskLevel": "MEDIUM",
                  "riskScore": 35,
                  "project": {
                    "buildTool": "maven",
                    "declaredJavaVersion": "8",
                    "springBootVersion": "2.7.18"
                  },
                  "findings": [
                    {"id": "java-8-baseline-target-17", "blockerCategory": "BASELINE", "severity": "RISK"},
                    {"id": "record-candidate", "blockerCategory": "LANGUAGE_MODERNIZATION", "severity": "OPPORTUNITY"}
                  ]
                }
                """);
        var outputFile = tempDir.resolve("portfolio.md");

        int exitCode = commandLine.execute(
                "portfolio",
                "--input",
                input.toString(),
                "--output",
                outputFile.toString());

        assertThat(exitCode).isZero();
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("Portfolio report written to");
        assertThat(Files.readString(outputFile))
                .contains("# Migration Portfolio Summary")
                .contains("Reports analyzed: 2")
                .contains("Risk levels:")
                .contains("## Top Blockers")
                .contains("FRAMEWORK_BASELINE")
                .contains("BASELINE")
                .contains("## Top Signals")
                .contains("AUTOMATION")
                .contains("LANGUAGE_MODERNIZATION");
    }

    @Test
    void portfolioCommandReturnsFriendlyErrorForMalformedJsonReport() throws Exception {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setErr(new PrintWriter(error, true));
        var input = tempDir.resolve("bad-reports");
        Files.createDirectories(input);
        Files.writeString(input.resolve("broken.json"), "{\"riskLevel\":\"HIGH\"}");

        int exitCode = commandLine.execute("portfolio", "--input", input.toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(error.toString(StandardCharsets.UTF_8))
                .contains("Error: Invalid JSON report")
                .doesNotContain("Exception")
                .doesNotContain("at dev.");
    }

    @Test
    void analyzeCommandReturnsFriendlyErrorForUnsupportedProject() throws Exception {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setErr(new PrintWriter(error, true));
        Files.writeString(tempDir.resolve("README.md"), "not a Java build");

        int exitCode = commandLine.execute(
                "analyze",
                "--path",
                tempDir.toString(),
                "--target",
                "21");

        assertThat(exitCode).isEqualTo(1);
        assertThat(error.toString(StandardCharsets.UTF_8))
                .contains("Error: No Maven or Gradle build file found")
                .doesNotContain("Exception")
                .doesNotContain("at dev.");
    }
}
