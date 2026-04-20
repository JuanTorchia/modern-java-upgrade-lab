package dev.modernjava.upgrade.core;

import java.util.List;

public class MarkdownReportRenderer {

    public String render(AnalysisRequest request, AnalysisResult result) {
        var metadata = result.metadata();
        var report = new StringBuilder();

        report.append("# Modern Java Upgrade Report\n\n");
        report.append("Project path: ").append(request.projectPath()).append('\n');
        report.append("Target Java version: ").append(request.targetJavaVersion()).append("\n\n");
        report.append("## Summary\n\n");
        report.append("Build tool: ").append(capitalize(metadata.buildTool())).append('\n');
        report.append("Declared Java version: ").append(metadata.declaredJavaVersion()).append('\n');
        report.append("Spring Boot version: ").append(metadata.springBootVersion()).append('\n');
        report.append("Dependencies: ").append(String.join(", ", metadata.dependencies())).append("\n\n");
        report.append("## Findings\n\n");

        List<Finding> findings = result.findings();
        if (findings.isEmpty()) {
            report.append("No findings were generated yet.");
            return report.toString();
        }

        for (Finding finding : findings) {
            report.append("### ").append(finding.title()).append("\n\n");
            report.append("- ID: ").append(finding.id()).append('\n');
            report.append("- Severity: ").append(finding.severity()).append('\n');
            report.append("- Area: ").append(finding.area()).append('\n');
            report.append("- Evidence: ").append(finding.evidence()).append('\n');
            report.append("- Recommendation: ").append(finding.recommendation()).append('\n');
            if (finding.openRewriteRecipe() != null && !finding.openRewriteRecipe().isBlank()) {
                report.append("- OpenRewrite recipe: ").append(finding.openRewriteRecipe()).append('\n');
            }
            report.append('\n');
        }

        return report.toString().stripTrailing();
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
