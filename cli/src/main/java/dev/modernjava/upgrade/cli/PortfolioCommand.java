package dev.modernjava.upgrade.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
        name = "portfolio",
        mixinStandardHelpOptions = true,
        description = "Aggregate generated JSON migration reports into an executive portfolio summary.")
public final class PortfolioCommand implements Callable<Integer> {

    private static final Pattern STRING_FIELD = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_FIELD = Pattern.compile("\"%s\"\\s*:\\s*(\\d+)");
    private static final Pattern FINDING_ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern FINDING_OBJECT = Pattern.compile("\\{[^{}]*\"id\"\\s*:\\s*\"([^\"]+)\"[^{}]*}");

    @Option(names = "--input", description = "Directory containing JSON reports.", required = true)
    private Path inputPath;

    @Option(names = "--output", description = "Write the portfolio Markdown report to this file.")
    private Path outputPath;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            var reports = readReports(inputPath);
            if (reports.isEmpty()) {
                throw new IllegalArgumentException("No JSON reports found under "
                        + inputPath.toAbsolutePath().normalize());
            }
            var markdown = renderMarkdown(reports);
            write(markdown);
            return 0;
        } catch (IllegalArgumentException | UncheckedIOException exception) {
            spec.commandLine().getErr().println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private static List<PortfolioReport> readReports(Path inputPath) {
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Portfolio input must be a directory: "
                    + inputPath.toAbsolutePath().normalize());
        }

        try (var paths = Files.walk(inputPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .map(PortfolioCommand::readReport)
                    .toList();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read portfolio input "
                    + inputPath.toAbsolutePath().normalize(), exception);
        }
    }

    private static PortfolioReport readReport(Path path) {
        try {
            var json = Files.readString(path);
            var target = intField(json, "targetJavaVersion");
            var riskLevel = requiredStringField(json, "riskLevel", path);
            var riskScore = intField(json, "riskScore");
            var buildTool = requiredStringField(json, "buildTool", path);
            var springBootVersion = nullableStringField(json, "springBootVersion");
            var blockerCategories = new ArrayList<String>();
            var signalCategories = new ArrayList<String>();
            var findingsJson = findingsArray(json);
            var findingMatcher = FINDING_OBJECT.matcher(findingsJson);
            while (findingMatcher.find()) {
                var findingJson = findingMatcher.group();
                var category = nullableStringField(findingJson, "blockerCategory");
                if (category == null) {
                    category = legacyBlockerCategory(findingMatcher.group(1));
                }
                var severity = nullableStringField(findingJson, "severity");
                if (isRiskSeverity(severity)) {
                    blockerCategories.add(category);
                } else {
                    signalCategories.add(category);
                }
            }
            if (blockerCategories.isEmpty() && signalCategories.isEmpty()) {
                var idMatcher = FINDING_ID.matcher(findingsJson);
                while (idMatcher.find()) {
                    blockerCategories.add(legacyBlockerCategory(idMatcher.group(1)));
                }
            }
            return new PortfolioReport(path.getFileName().toString(), target, riskLevel, riskScore,
                    buildTool, springBootVersion, List.copyOf(blockerCategories), List.copyOf(signalCategories));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read JSON report " + path.toAbsolutePath().normalize(), exception);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid JSON report " + path.toAbsolutePath().normalize()
                    + ": " + exception.getMessage(), exception);
        }
    }

    private static String renderMarkdown(List<PortfolioReport> reports) {
        var markdown = new StringBuilder();
        markdown.append("# Migration Portfolio Summary\n\n");
        markdown.append("## Executive Summary\n\n");
        markdown.append("- Reports analyzed: ").append(reports.size()).append('\n');
        appendGroupedCount(markdown, "Risk levels", reports.stream()
                .collect(Collectors.groupingBy(PortfolioReport::riskLevel, LinkedHashMap::new, Collectors.counting())));
        appendGroupedCount(markdown, "Target Java versions", reports.stream()
                .collect(Collectors.groupingBy(report -> "Java " + report.targetJavaVersion(), LinkedHashMap::new, Collectors.counting())));
        appendGroupedCount(markdown, "Build tools", reports.stream()
                .collect(Collectors.groupingBy(PortfolioReport::buildTool, LinkedHashMap::new, Collectors.counting())));
        appendGroupedCount(markdown, "Spring Boot major", reports.stream()
                .collect(Collectors.groupingBy(PortfolioCommand::springBootMajor, LinkedHashMap::new, Collectors.counting())));

        markdown.append("\n## Top Blockers\n\n");
        var blockerCounts = reports.stream()
                .flatMap(report -> report.blockerCategories().stream())
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        appendRankedCounts(markdown, blockerCounts);

        markdown.append("\n## Top Signals\n\n");
        var signalCounts = reports.stream()
                .flatMap(report -> report.signalCategories().stream())
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        appendRankedCounts(markdown, signalCounts);

        markdown.append("\n## Reports\n\n");
        markdown.append("| Report | Risk | Score | Target | Build | Spring Boot |\n");
        markdown.append("| --- | --- | ---: | ---: | --- | --- |\n");
        for (PortfolioReport report : reports) {
            markdown.append("| ")
                    .append(report.name())
                    .append(" | ")
                    .append(report.riskLevel())
                    .append(" | ")
                    .append(report.riskScore())
                    .append(" | ")
                    .append(report.targetJavaVersion())
                    .append(" | ")
                    .append(report.buildTool())
                    .append(" | ")
                    .append(report.springBootVersion() == null ? "Unknown" : report.springBootVersion())
                    .append(" |\n");
        }
        return markdown.toString().stripTrailing();
    }

    private static void appendGroupedCount(StringBuilder markdown, String label, Map<String, Long> values) {
        markdown.append("- ").append(label).append(": ");
        markdown.append(values.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ")));
        markdown.append('\n');
    }

    private static void appendRankedCounts(StringBuilder markdown, Map<String, Long> values) {
        if (values.isEmpty()) {
            markdown.append("- None\n");
            return;
        }
        values.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(10)
                .forEach(entry -> markdown.append("- ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append('\n'));
    }

    private static boolean isRiskSeverity(String severity) {
        return severity == null || severity.equals("BLOCKER") || severity.equals("RISK");
    }

    private static String springBootMajor(PortfolioReport report) {
        if (report.springBootVersion() == null || report.springBootVersion().isBlank()) {
            return "Unknown";
        }
        var first = report.springBootVersion().split("\\.")[0];
        return "Spring Boot " + first;
    }

    private static String findingsArray(String json) {
        var marker = "\"findings\"";
        var start = json.indexOf(marker);
        if (start < 0) {
            return "";
        }
        var arrayStart = json.indexOf('[', start);
        var nextSection = json.indexOf("\"analysisMetadata\"", arrayStart);
        if (arrayStart < 0) {
            return "";
        }
        if (nextSection < 0) {
            return json.substring(arrayStart);
        }
        return json.substring(arrayStart, nextSection);
    }

    private static String legacyBlockerCategory(String id) {
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
        if (id.contains("openrewrite")) {
            return "AUTOMATION";
        }
        return "OTHER";
    }

    private void write(String markdown) {
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
            spec.commandLine().getOut().println("Portfolio report written to "
                    + outputPath.toAbsolutePath().normalize());
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write portfolio report to "
                    + outputPath.toAbsolutePath().normalize(), exception);
        }
    }

    private static String requiredStringField(String json, String field, Path path) {
        var value = nullableStringField(json, field);
        if (value == null) {
            throw new IllegalArgumentException("missing required field `" + field + "` in " + path.getFileName());
        }
        return value;
    }

    private static String nullableStringField(String json, String field) {
        var matcher = Pattern.compile(STRING_FIELD.pattern().formatted(field)).matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static int intField(String json, String field) {
        var matcher = Pattern.compile(NUMBER_FIELD.pattern().formatted(field)).matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing required numeric field `" + field + "`");
        }
        return Integer.parseInt(matcher.group(1));
    }

    private record PortfolioReport(
            String name,
            int targetJavaVersion,
            String riskLevel,
            int riskScore,
            String buildTool,
            String springBootVersion,
            List<String> blockerCategories,
            List<String> signalCategories) {
    }
}
