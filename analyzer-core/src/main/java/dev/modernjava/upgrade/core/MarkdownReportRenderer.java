package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class MarkdownReportRenderer {
    private static final Pattern FIRST_INTEGER = Pattern.compile("\\d+");

    public String render(AnalysisRequest request, AnalysisResult result) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        var metadata = result.metadata();
        var report = new StringBuilder();

        report.append("# Modern Java Upgrade Report\n\n");
        report.append("Project path: `").append(displayPath(request.projectPath())).append("`\n\n");
        appendExecutiveSummary(report, result);
        report.append("## Project Summary\n\n");
        report.append("- Build tool: ").append(displayBuildTool(metadata.buildTool())).append('\n');
        report.append("- Declared Java version: ").append(displayValue(metadata.declaredJavaVersion())).append('\n');
        report.append("- Target Java version: ").append(result.targetJavaVersion()).append('\n');
        report.append("- Migration status: ")
                .append(displayMigrationStatus(metadata.declaredJavaVersion(), result.targetJavaVersion()))
                .append('\n');
        report.append("- Spring Boot version: ").append(displayValue(metadata.springBootVersion())).append('\n');
        appendRiskAssessment(report, result.riskAssessment());
        appendBuildReadiness(report, metadata.buildReadiness());
        appendAnalysisMetadata(report, request, result);
        appendDependencyBaselines(report, metadata.dependencyBaselines());
        appendDiagnostics(report, metadata.diagnostics());
        appendMigrationBlockers(report, result.findings());
        appendWorkItems(report, result.workItems());
        appendSuggestedCommands(report, result.workItems());
        appendMigrationPlan(report, metadata, result.targetJavaVersion());

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

    private static void appendDiagnostics(StringBuilder report, List<InspectorDiagnostic> diagnostics) {
        if (diagnostics.isEmpty()) {
            return;
        }

        report.append("\n## Inspection Diagnostics\n\n");
        for (InspectorDiagnostic diagnostic : diagnostics) {
            report.append("### [")
                    .append(displayValue(diagnostic.severity().name()))
                    .append("] ")
                    .append(displayValue(diagnostic.source()))
                    .append("\n\n");
            report.append("- Message: ").append(displayText(diagnostic.message())).append('\n');
            if (diagnostic.path() != null) {
                report.append("- Path: `").append(displayText(diagnostic.path().toString())).append("`\n");
            }
        }
    }

    private static void appendExecutiveSummary(StringBuilder report, AnalysisResult result) {
        var blockers = result.findings().stream()
                .filter(finding -> finding.severity() == FindingSeverity.RISK)
                .count();
        report.append("## Executive Summary\n\n");
        report.append("- Readiness risk: ").append(result.riskAssessment().level())
                .append(" (").append(result.riskAssessment().score()).append("/100)\n");
        report.append("- Migration blockers: ").append(blockers).append('\n');
        report.append("- Recommended work items: ").append(result.workItems().size()).append('\n');
        report.append("- Decision: ")
                .append(blockers > 0
                        ? "Do not treat this as a language-only upgrade; resolve blockers before rollout."
                        : "Proceed with baseline validation before optional modernization.")
                .append("\n\n");
    }

    private static void appendRiskAssessment(StringBuilder report, RiskAssessment riskAssessment) {
        report.append("\n## Risk Assessment\n\n");
        report.append("- Risk level: ").append(riskAssessment.level()).append('\n');
        report.append("- Risk score: ").append(riskAssessment.score()).append("/100\n");
        for (String reason : riskAssessment.reasons()) {
            report.append("- Reason: ").append(displayText(reason)).append('\n');
        }
    }

    private static void appendBuildReadiness(StringBuilder report, BuildReadiness buildReadiness) {
        report.append("\n## Build Readiness\n\n");
        report.append("- Build wrapper present: ").append(buildReadiness.buildWrapperPresent() ? "Yes" : "No").append('\n');
        report.append("- CI provider: ").append(displayText(buildReadiness.ciProvider())).append('\n');
        report.append("- CI evidence: `").append(displayText(buildReadiness.ciEvidence())).append("`\n");
        report.append("- Suggested test command: `").append(displayText(buildReadiness.suggestedTestCommand())).append("`\n");
    }

    private static void appendWorkItems(StringBuilder report, List<WorkItem> workItems) {
        if (workItems.isEmpty()) {
            return;
        }

        report.append("\n## Recommended Work Items\n\n");
        for (int index = 0; index < workItems.size(); index++) {
            var item = workItems.get(index);
            report.append("### [")
                    .append(displayText(item.category()))
                    .append("] ")
                    .append(displayText(item.title()))
                    .append("\n\n");
            report.append("- Rationale: ").append(displayText(item.rationale())).append('\n');
            report.append("- Priority: ").append(displayText(item.priority())).append('\n');
            report.append("- Phase: ").append(displayText(item.phase())).append('\n');
            if (item.command() != null) {
                report.append("- Command: `").append(displayText(item.command())).append("`\n");
            }
            if (index + 1 < workItems.size()) {
                report.append('\n');
            }
        }
    }

    private static void appendMigrationBlockers(StringBuilder report, List<Finding> findings) {
        var blockers = findings.stream()
                .filter(finding -> finding.severity() == FindingSeverity.RISK)
                .toList();
        if (blockers.isEmpty()) {
            return;
        }

        report.append("\n## Migration Blockers\n\n");
        for (Finding finding : blockers) {
            report.append("- [")
                    .append(displayText(finding.category().name()))
                    .append("] ")
                    .append(displayText(finding.title()))
                    .append(" - ")
                    .append(displayText(finding.evidence()))
                    .append('\n');
        }
    }

    private static void appendSuggestedCommands(StringBuilder report, List<WorkItem> workItems) {
        var commands = workItems.stream()
                .filter(item -> item.command() != null)
                .toList();
        if (commands.isEmpty()) {
            return;
        }

        report.append("\n## Suggested Commands\n\n");
        for (WorkItem item : commands) {
            report.append("- `").append(displayText(item.command())).append("`").append('\n');
        }
    }

    private static void appendDependencyBaselines(StringBuilder report, List<DependencyBaseline> baselines) {
        if (baselines.isEmpty()) {
            return;
        }

        report.append("\n## Dependency & Plugin Baselines\n\n");
        report.append("| Category | Name | Version | Evidence |\n");
        report.append("| --- | --- | --- | --- |\n");
        for (DependencyBaseline baseline : baselines) {
            report.append("| ")
                    .append(displayTableText(baseline.category()))
                    .append(" | ")
                    .append(displayTableText(baseline.name()))
                    .append(" | ")
                    .append(displayTableText(baseline.version()))
                    .append(" | `")
                    .append(displayText(baseline.evidence()))
                    .append("` |\n");
        }
    }

    private static void appendAnalysisMetadata(StringBuilder report, AnalysisRequest request, AnalysisResult result) {
        var metadata = result.analysisMetadata();
        report.append("\n## Analysis Metadata\n\n");
        report.append("- Analyzer version: ").append(displayText(metadata.analyzerVersion())).append('\n');
        report.append("- Generated at: ").append(metadata.generatedAt() == null ? "Unknown" : metadata.generatedAt()).append('\n');
        report.append("- Source path: `").append(displayPath(request.projectPath())).append("`\n");
        report.append("- Git commit: `").append(displayText(metadata.gitCommit())).append("`\n");
        report.append("- Git branch: `").append(displayText(metadata.gitBranch())).append("`\n");
        report.append("- Target Java: ").append(result.targetJavaVersion()).append('\n');
    }

    private static void appendMigrationPlan(StringBuilder report, ProjectMetadata metadata, int targetJavaVersion) {
        report.append("\n## Migration Plan\n\n");
        report.append("### Phase 1: Baseline\n\n");
        report.append("- Confirm the current Java version in local development and CI.\n");
        report.append("- Run the full test suite before changing the Java target.\n");
        report.append("- Make compiler, toolchain, and runtime configuration explicit.\n\n");
        report.append("### Phase 2: Framework Compatibility\n\n");
        appendFrameworkCompatibilityPlan(report, metadata.springBootVersion(), targetJavaVersion);
        report.append("\n### Phase 3: Automated Changes\n\n");
        report.append("- Run suggested OpenRewrite recipes in a dedicated branch.\n");
        report.append("- Review generated diffs and datatables before merging.\n\n");
        report.append("### Phase 4: Manual Review\n\n");
        report.append("- Review source modernization candidates after the migration baseline is stable.\n");
        report.append("- Keep optional refactors out of the baseline migration branch.\n\n");
        report.append("### Out-of-scope Modernization\n\n");
        report.append("- Do not combine broad DTO/record refactors with runtime rollout.\n");
        report.append("- Do not auto-rewrite concurrency primitives without lifecycle validation.\n\n");
        report.append("### Phase 5: Rollout\n\n");
        report.append("- Validate CI, container images, runtime flags, observability, and rollback paths.\n");
        report.append("- Roll out the runtime upgrade separately from broad application refactors.\n");
    }

    private static void appendFrameworkCompatibilityPlan(
            StringBuilder report, String springBootVersion, int targetJavaVersion) {
        if (springBootVersion != null && springBootVersion.startsWith("2.")) {
            report.append("- Validate Spring Boot ")
                    .append(displayValue(springBootVersion))
                    .append(" support before moving to Java ")
                    .append(targetJavaVersion)
                    .append(".\n");
            report.append("- Move to Spring Boot 2.7.x first when staying on the Spring Boot 2 line.\n");
            report.append("- Treat Spring Boot 3.x as a separate migration because it introduces Jakarta namespace changes.\n");
            return;
        }

        if (springBootVersion != null) {
            report.append("- Validate Spring Boot ")
                    .append(displayValue(springBootVersion))
                    .append(" against Java ")
                    .append(targetJavaVersion)
                    .append(" before runtime rollout.\n");
            report.append("- Keep framework upgrades separate from optional language refactors.\n");
            return;
        }

        report.append("- Identify the framework baseline before runtime rollout.\n");
        report.append("- Treat unknown framework versions as a migration planning risk.\n");
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

    private static String displayTableText(String value) {
        return displayText(value).replace("|", "\\|");
    }

    private static String displayMigrationStatus(String declaredJavaVersion, int targetJavaVersion) {
        var declaredMajor = parseJavaMajorVersion(declaredJavaVersion);
        if (declaredMajor.isEmpty()) {
            return "Baseline unknown; verify Java version before planning Java " + targetJavaVersion;
        }

        var declared = declaredMajor.getAsInt();
        if (declared == targetJavaVersion) {
            return "Already at target Java " + targetJavaVersion;
        }
        if (declared > targetJavaVersion) {
            return "Target Java " + targetJavaVersion + " is below declared Java " + declared;
        }
        return "Upgrade required (Java " + declared + " -> " + targetJavaVersion + ")";
    }

    private static OptionalInt parseJavaMajorVersion(String value) {
        if (value == null || value.isBlank()) {
            return OptionalInt.empty();
        }

        var normalized = sanitize(value).toLowerCase();
        if (normalized.equals("unknown")) {
            return OptionalInt.empty();
        }
        if (normalized.startsWith("1.")) {
            var legacyMatcher = FIRST_INTEGER.matcher(normalized.substring(2));
            if (legacyMatcher.find()) {
                return OptionalInt.of(Integer.parseInt(legacyMatcher.group()));
            }
        }

        var matcher = FIRST_INTEGER.matcher(normalized);
        if (matcher.find()) {
            return OptionalInt.of(Integer.parseInt(matcher.group()));
        }
        return OptionalInt.empty();
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
