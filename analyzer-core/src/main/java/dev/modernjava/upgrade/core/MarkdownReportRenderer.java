package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MarkdownReportRenderer {

    public String render(AnalysisRequest request, AnalysisResult result) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        var metadata = result.metadata();
        var report = new StringBuilder();

        report.append("# Modern Java Upgrade Report\n\n");
        report.append("Project path: `").append(displayPath(request.projectPath())).append("`\n\n");
        report.append("## Project Summary\n\n");
        report.append("- Build tool: ").append(displayBuildTool(metadata.buildTool())).append('\n');
        report.append("- Declared Java version: ").append(displayValue(metadata.declaredJavaVersion())).append('\n');
        report.append("- Target Java version: ").append(result.targetJavaVersion()).append('\n');
        report.append("- Spring Boot version: ").append(displayValue(metadata.springBootVersion())).append('\n');

        List<Finding> findings = result.findings();
        if (findings.isEmpty()) {
            report.append('\n');
            report.append("No findings were generated yet.");
            return report.toString();
        }

        var findingsByCategory = groupFindingsByCategory(findings);
        for (var section : sectionTitles().entrySet()) {
            var sectionFindings = findingsByCategory.getOrDefault(section.getKey(), List.of());
            if (sectionFindings.isEmpty()) {
                continue;
            }
            report.append("\n## ").append(section.getValue()).append("\n\n");
            for (Finding finding : sectionFindings) {
                appendFinding(report, finding);
            }
        }

        return report.toString().stripTrailing();
    }

    private static void appendFinding(StringBuilder report, Finding finding) {
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
            report.append("- OpenRewrite command: `")
                    .append(openRewriteMavenCommand(recipe))
                    .append("`\n");
        }
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

    private static Map<FindingCategory, List<Finding>> groupFindingsByCategory(List<Finding> findings) {
        var grouped = new EnumMap<FindingCategory, List<Finding>>(FindingCategory.class);
        for (Finding finding : findings) {
            grouped.computeIfAbsent(finding.category(), ignored -> new ArrayList<>()).add(finding);
        }
        return grouped;
    }

    private static Map<FindingCategory, String> sectionTitles() {
        var sections = new LinkedHashMap<FindingCategory, String>();
        sections.put(FindingCategory.RISK, "Migration Risks");
        sections.put(FindingCategory.BUILD, "Build & Tooling");
        sections.put(FindingCategory.FRAMEWORK, "Framework Compatibility");
        sections.put(FindingCategory.LANGUAGE, "Language Modernization");
        sections.put(FindingCategory.CONCURRENCY, "Concurrency");
        sections.put(FindingCategory.PERFORMANCE, "Performance");
        sections.put(FindingCategory.OBSERVABILITY, "Observability");
        sections.put(FindingCategory.AUTOMATION, "Automation Suggestions");
        sections.put(FindingCategory.BASELINE, "Baseline & Planning");
        return sections;
    }

    private static String openRewriteMavenCommand(String recipe) {
        return "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run "
                + "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE "
                + "-Drewrite.activeRecipes=" + recipe + " "
                + "-Drewrite.exportDatatables=true";
    }
}
