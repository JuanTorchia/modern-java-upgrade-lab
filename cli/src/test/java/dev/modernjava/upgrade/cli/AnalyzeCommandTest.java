package dev.modernjava.upgrade.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
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
    void analyzeCommandPrintsSkeletonReport() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        CommandLine commandLine = new CommandLine(new ModernJavaUpgradeLabApp());

        try {
            int exitCode = commandLine.execute("analyze", "--target", "21");

            assertThat(exitCode).isZero();
            String text = output.toString(StandardCharsets.UTF_8);
            assertThat(text).contains("# Modern Java Upgrade Report");
            assertThat(text).contains("Analyzer wiring will be connected in the next task.");
        } finally {
            System.setOut(originalOut);
        }
    }
}
