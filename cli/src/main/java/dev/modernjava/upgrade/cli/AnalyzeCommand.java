package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.build.ProjectInspector;
import dev.modernjava.upgrade.core.AnalysisRequest;
import dev.modernjava.upgrade.core.DefaultAnalyzer;
import dev.modernjava.upgrade.core.MarkdownReportRenderer;
import dev.modernjava.upgrade.core.ProjectMetadata;
import dev.modernjava.upgrade.core.SourcePatternScanner;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report.")
public final class AnalyzeCommand implements Callable<Integer> {

    @Option(names = "--path", description = "Project path to analyze.", defaultValue = ".")
    private Path path;

    @Option(names = "--target", description = "Target Java version.", required = true)
    private int targetVersion;

    @Option(names = "--output", description = "Write the Markdown report to this file instead of stdout.")
    private Path outputPath;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            var request = new AnalysisRequest(path, targetVersion);
            ProjectMetadata inspectedMetadata = new ProjectInspector().inspect(path);
            var sourcePatterns = new SourcePatternScanner().scan(path);
            var metadata = new ProjectMetadata(
                    inspectedMetadata.buildTool(),
                    inspectedMetadata.declaredJavaVersion(),
                    inspectedMetadata.springBootVersion(),
                    inspectedMetadata.dependencies(),
                    inspectedMetadata.buildPlugins(),
                    sourcePatterns);
            var result = new DefaultAnalyzer(metadata).analyze(request);
            var markdown = new MarkdownReportRenderer().render(request, result);
            writeReport(markdown);
            return 0;
        } catch (IllegalArgumentException | UncheckedIOException exception) {
            spec.commandLine().getErr().println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private void writeReport(String markdown) {
        if (outputPath == null) {
            spec.commandLine().getOut().println(markdown);
            return;
        }

        try {
            var parent = outputPath.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputPath, markdown);
            spec.commandLine().getOut().println("Report written to " + outputPath.toAbsolutePath().normalize());
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write report to "
                    + outputPath.toAbsolutePath().normalize(), exception);
        }
    }
}
