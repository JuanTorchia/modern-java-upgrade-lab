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
                .contains("Build tool: Gradle")
                .contains("Target Java version: 25");
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
