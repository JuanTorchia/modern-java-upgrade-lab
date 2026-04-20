package dev.modernjava.upgrade.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class AnalyzeCommandTest {

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
}
