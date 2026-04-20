package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class MarkdownReportRenderer {

    public String render(AnalysisRequest request, AnalysisResult result) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        var metadata = result.metadata();
        var report = new StringBuilder();

        report.append("# Modern Java Upgrade Report\n\n");
        report.append("Project path: `").append(displayPath(request.projectPath())).append("`\n\n");
        report.append("## Summary\n\n");
        report.append("- Build tool: ").append(displayBuildTool(metadata.buildTool())).append('\n');
        report.append("- Declared Java version: ").append(displayValue(metadata.declaredJavaVersion())).append('\n');
        report.append("- Target Java version: ").append(result.targetJavaVersion()).append('\n');
        report.append("- Spring Boot version: ").append(displayValue(metadata.springBootVersion())).append("\n\n");
        report.append("## Findings\n\n");

        List<Finding> findings = result.findings();
        if (findings.isEmpty()) {
            report.append("No findings were generated yet.");
            return report.toString();
        }

        for (Finding finding : findings) {
            report.append("### [")
                    .append(displayValue(finding.severity().name()))
                    .append("] ")
                    .append(displayValue(finding.title()))
                    .append("\n\n");
            report.append("- Area: ").append(displayValue(finding.area())).append('\n');
            report.append("- Evidence: ").append(displayText(finding.evidence())).append('\n');
            report.append("- Recommendation: ").append(displayText(finding.recommendation())).append('\n');
            var recipe = finding.openRewriteRecipe();
            if (recipe != null && !recipe.isBlank()) {
                report.append("- OpenRewrite recipe: `").append(displayText(recipe)).append("`\n");
            }
            report.append('\n');
        }

        return report.toString().stripTrailing();
    }

    private static String displayBuildTool(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private static String displayValue(String value) {
        return value == null || value.isBlank() ? "Unknown" : sanitize(value);
    }

    private static String displayText(String value) {
        return value == null || value.isBlank() ? "Unknown" : sanitize(value);
    }

    private static String sanitize(String value) {
        return value.replace('\r', ' ').replace('\n', ' ').replaceAll(" +", " ").trim();
    }

    private static String displayPath(Path path) {
        return path == null ? "Unknown" : path.toAbsolutePath().normalize().toString();
    }
}
