package dev.modernjava.upgrade.core;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class JsonReportRenderer {

    public String render(AnalysisRequest request, AnalysisResult result) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        var metadata = result.metadata();
        var json = new StringBuilder();

        json.append("{\n");
        field(json, 1, "schemaVersion", "1.0", true);
        field(json, 1, "targetJavaVersion", result.targetJavaVersion(), true);
        field(json, 1, "riskLevel", result.riskAssessment().level().name(), true);
        field(json, 1, "riskScore", result.riskAssessment().score(), true);
        array(json, 1, "riskReasons", result.riskAssessment().reasons(), true);
        buildReadiness(json, metadata.buildReadiness(), true);
        objectStart(json, 1, "project", true);
        field(json, 2, "path", displayPath(request.projectPath()), true);
        field(json, 2, "buildTool", metadata.buildTool(), true);
        field(json, 2, "declaredJavaVersion", metadata.declaredJavaVersion(), true);
        field(json, 2, "springBootVersion", metadata.springBootVersion(), false);
        objectEnd(json, 1, true);
        dependencyBaselines(json, metadata.dependencyBaselines(), true);
        workItems(json, result.workItems(), true);
        findings(json, result.findings(), true);
        analysisMetadata(json, result.analysisMetadata(), false);
        json.append("}\n");

        return json.toString();
    }

    private static void dependencyBaselines(StringBuilder json, List<DependencyBaseline> baselines, boolean comma) {
        indent(json, 1).append("\"dependencyBaselines\": [");
        if (!baselines.isEmpty()) {
            json.append('\n');
            for (int index = 0; index < baselines.size(); index++) {
                var baseline = baselines.get(index);
                indent(json, 2).append("{\n");
                field(json, 3, "category", baseline.category(), true);
                field(json, 3, "name", baseline.name(), true);
                field(json, 3, "version", baseline.version(), true);
                field(json, 3, "evidence", baseline.evidence(), false);
                indent(json, 2).append('}');
                if (index + 1 < baselines.size()) {
                    json.append(',');
                }
                json.append('\n');
            }
            indent(json, 1);
        }
        json.append(']');
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void buildReadiness(StringBuilder json, BuildReadiness buildReadiness, boolean comma) {
        objectStart(json, 1, "buildReadiness", true);
        field(json, 2, "buildWrapperPresent", buildReadiness.buildWrapperPresent(), true);
        field(json, 2, "ciProvider", buildReadiness.ciProvider(), true);
        field(json, 2, "ciEvidence", buildReadiness.ciEvidence(), true);
        field(json, 2, "suggestedTestCommand", buildReadiness.suggestedTestCommand(), false);
        objectEnd(json, 1, comma);
    }

    private static void workItems(StringBuilder json, List<WorkItem> workItems, boolean comma) {
        indent(json, 1).append("\"workItems\": [");
        if (!workItems.isEmpty()) {
            json.append('\n');
            for (int index = 0; index < workItems.size(); index++) {
                var item = workItems.get(index);
                indent(json, 2).append("{\n");
                field(json, 3, "id", item.id(), true);
                field(json, 3, "category", item.category(), true);
                field(json, 3, "title", item.title(), true);
                field(json, 3, "rationale", item.rationale(), true);
                field(json, 3, "command", item.command(), true);
                field(json, 3, "priority", item.priority(), true);
                field(json, 3, "phase", item.phase(), false);
                indent(json, 2).append('}');
                if (index + 1 < workItems.size()) {
                    json.append(',');
                }
                json.append('\n');
            }
            indent(json, 1);
        }
        json.append(']');
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void findings(StringBuilder json, List<Finding> findings, boolean comma) {
        indent(json, 1).append("\"findings\": [");
        if (!findings.isEmpty()) {
            json.append('\n');
            for (int index = 0; index < findings.size(); index++) {
                var finding = findings.get(index);
                indent(json, 2).append("{\n");
                field(json, 3, "id", finding.id(), true);
                field(json, 3, "category", finding.category().name(), true);
                field(json, 3, "blockerCategory", blockerCategory(finding), true);
                field(json, 3, "severity", finding.severity().name(), true);
                field(json, 3, "area", finding.area(), true);
                field(json, 3, "title", finding.title(), true);
                field(json, 3, "evidence", finding.evidence(), true);
                field(json, 3, "recommendation", finding.recommendation(), true);
                field(json, 3, "openRewriteRecipe", finding.openRewriteRecipe(), false);
                indent(json, 2).append('}');
                if (index + 1 < findings.size()) {
                    json.append(',');
                }
                json.append('\n');
            }
            indent(json, 1);
        }
        json.append(']');
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void analysisMetadata(StringBuilder json, AnalysisMetadata metadata, boolean comma) {
        objectStart(json, 1, "analysisMetadata", false);
        field(json, 2, "analyzerVersion", metadata.analyzerVersion(), true);
        field(json, 2, "generatedAt", displayInstant(metadata.generatedAt()), true);
        field(json, 2, "gitCommit", metadata.gitCommit(), true);
        field(json, 2, "gitBranch", metadata.gitBranch(), false);
        objectEnd(json, 1, comma);
    }

    private static String blockerCategory(Finding finding) {
        var id = finding.id();
        if (id.contains("removed-java-ee")) {
            return "JAVA_EE_REMOVED";
        }
        if (id.contains("reflective-access")) {
            return "REFLECTIVE_ACCESS";
        }
        if (id.contains("runtime-image") || id.contains("runtime-baseline")) {
            return "RUNTIME_IMAGE";
        }
        if (id.contains("spring-boot")) {
            return "FRAMEWORK_BASELINE";
        }
        if (id.contains("test-plugin") || id.contains("maven-compiler") || id.contains("gradle-wrapper")) {
            return "BUILD_PLUGIN";
        }
        if (id.contains("legacy-dependency")) {
            return "DEPENDENCY_COMPATIBILITY";
        }
        if (id.contains("jdk-internal")) {
            return "JDK_INTERNAL_API";
        }
        if (id.contains("openrewrite")) {
            return "AUTOMATION";
        }
        if (finding.category() == FindingCategory.CONCURRENCY) {
            return "CONCURRENCY";
        }
        if (finding.category() == FindingCategory.LANGUAGE) {
            return "LANGUAGE_MODERNIZATION";
        }
        return finding.category().name();
    }

    private static void objectStart(StringBuilder json, int level, String name, boolean commaBeforeNext) {
        indent(json, level).append('"').append(name).append("\": {\n");
    }

    private static void objectEnd(StringBuilder json, int level, boolean comma) {
        indent(json, level).append('}');
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void array(StringBuilder json, int level, String name, List<String> values, boolean comma) {
        indent(json, level).append('"').append(name).append("\": [");
        for (int index = 0; index < values.size(); index++) {
            json.append('"').append(escape(values.get(index))).append('"');
            if (index + 1 < values.size()) {
                json.append(", ");
            }
        }
        json.append(']');
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void field(StringBuilder json, int level, String name, String value, boolean comma) {
        indent(json, level).append('"').append(name).append("\": ");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escape(value)).append('"');
        }
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void field(StringBuilder json, int level, String name, int value, boolean comma) {
        indent(json, level).append('"').append(name).append("\": ").append(value);
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void field(StringBuilder json, int level, String name, boolean value, boolean comma) {
        indent(json, level).append('"').append(name).append("\": ").append(value);
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String displayPath(Path path) {
        return path == null ? null : path.toAbsolutePath().normalize().toString();
    }

    private static String displayInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private static StringBuilder indent(StringBuilder json, int level) {
        return json.append("  ".repeat(level));
    }
}
