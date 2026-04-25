package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.build.ProjectInspector;
import dev.modernjava.upgrade.core.AnalysisRequest;
import dev.modernjava.upgrade.core.AnalysisResult;
import dev.modernjava.upgrade.core.DefaultAnalyzer;
import dev.modernjava.upgrade.core.JsonReportRenderer;
import dev.modernjava.upgrade.core.MarkdownReportRenderer;
import dev.modernjava.upgrade.core.ProjectMetadata;
import dev.modernjava.upgrade.core.RiskLevel;
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

    @Option(names = "--output", description = "Write the report to this file instead of stdout.")
    private Path outputPath;

    @Option(names = "--format", description = "Output format: markdown or json.", defaultValue = "markdown")
    private String outputFormat;

    @Option(names = "--fail-on-risk", description = "Return exit code 2 when risk is at or above LOW, MEDIUM, or HIGH.")
    private String failOnRisk;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            var request = new AnalysisRequest(path, targetVersion);
            ProjectMetadata inspectedMetadata = new ProjectInspector().inspect(path);
            var sourcePatterns = new SourcePatternScanner().scan(path);
            var buildReadiness = new BuildReadinessCollector().collect(path, inspectedMetadata.buildTool());
            var metadata = new ProjectMetadata(
                    inspectedMetadata.buildTool(),
                    inspectedMetadata.declaredJavaVersion(),
                    inspectedMetadata.springBootVersion(),
                    inspectedMetadata.dependencies(),
                    inspectedMetadata.buildPlugins(),
                    inspectedMetadata.compilerArgs(),
                    sourcePatterns,
                    inspectedMetadata.dependencyBaselines(),
                    buildReadiness,
                    inspectedMetadata.diagnostics());
            var analysisMetadata = new AnalysisMetadataCollector().collect(path);
            var result = new DefaultAnalyzer(metadata).analyze(request, analysisMetadata);
            var report = renderReport(request, result);
            writeReport(report);
            if (failsRiskThreshold(result)) {
                spec.commandLine().getErr().println("Risk threshold failed: "
                        + result.riskAssessment().level()
                        + " >= "
                        + parseRiskLevel(failOnRisk));
                return 2;
            }
            return 0;
        } catch (IllegalArgumentException | UncheckedIOException exception) {
            spec.commandLine().getErr().println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private String renderReport(AnalysisRequest request, AnalysisResult result) {
        return switch (outputFormat.toLowerCase()) {
            case "markdown", "md" -> new MarkdownReportRenderer().render(request, result);
            case "json" -> new JsonReportRenderer().render(request, result);
            default -> throw new IllegalArgumentException("Unsupported output format: " + outputFormat);
        };
    }

    private boolean failsRiskThreshold(AnalysisResult result) {
        if (failOnRisk == null || failOnRisk.isBlank()) {
            return false;
        }
        return riskRank(result.riskAssessment().level()) >= riskRank(parseRiskLevel(failOnRisk));
    }

    private static RiskLevel parseRiskLevel(String value) {
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unsupported risk threshold: " + value
                    + " (expected LOW, MEDIUM, or HIGH)", exception);
        }
    }

    private static int riskRank(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
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
