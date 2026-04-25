package dev.modernjava.upgrade.core;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Pattern;

final class RiskAssessor {
    private static final Pattern FIRST_INTEGER = Pattern.compile("\\d+");

    RiskAssessment assess(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {
        var score = 0;
        var reasons = new ArrayList<String>();
        var declaredJava = parseMajor(metadata.declaredJavaVersion());

        if (declaredJava.isPresent() && declaredJava.getAsInt() < targetJavaVersion) {
            score += 15;
            reasons.add("Declared Java " + declaredJava.getAsInt() + " targets Java " + targetJavaVersion);
        }

        if (declaredJava.isPresent() && declaredJava.getAsInt() <= 8 && targetJavaVersion == 11) {
            score += 10;
            reasons.add("Java 8 to Java 11 requires removed-module and illegal-access validation");
        }

        if (declaredJava.isPresent() && declaredJava.getAsInt() <= 11 && targetJavaVersion >= 21) {
            score += 20;
        }
        if (declaredJava.isPresent() && declaredJava.getAsInt() <= 8 && targetJavaVersion >= 21) {
            score += 20;
            reasons.add("Java 8 to Java " + targetJavaVersion + " crosses multiple LTS baselines");
        }

        var springBootMajorMinor = parseSpringBootMajorMinor(metadata.springBootVersion());
        if (targetJavaVersion >= 21 && springBootMajorMinor.isEmpty()) {
            score += 15;
            reasons.add("Spring Boot baseline is unknown for Java " + targetJavaVersion + " planning");
        } else if (targetJavaVersion >= 21
                && springBootMajorMinor.isPresent()
                && springBootMajorMinor.get().major() == 2
                && springBootMajorMinor.get().minor() < 7) {
            score += 25;
            reasons.add("Spring Boot " + metadata.springBootVersion()
                    + " is below the safer 2.7.x Java 21 staging baseline");
        }

        var riskFindings = findings.stream()
                .filter(finding -> finding.severity() == FindingSeverity.RISK)
                .count();
        if (riskFindings > 0) {
            score += Math.min(30, (int) riskFindings * 10);
            reasons.add("Report contains " + riskFindings + " risk-severity finding(s)");
        }

        for (DependencyBaseline baseline : metadata.dependencyBaselines()) {
            addBaselineRisk(baseline, targetJavaVersion, reasons);
        }
        score += baselineRiskScore(metadata.dependencyBaselines(), targetJavaVersion);

        score = Math.min(100, score);
        return new RiskAssessment(levelFor(score), score, reasons);
    }

    private static void addBaselineRisk(DependencyBaseline baseline, int targetJavaVersion, List<String> reasons) {
        if ("Gradle wrapper".equals(baseline.name())
                && targetJavaVersion >= 21
                && parseMajor(baseline.version()).orElse(99) < 7) {
            reasons.add("Gradle wrapper " + baseline.version() + " should be validated before Java "
                    + targetJavaVersion + " builds");
        }
        if ("Runtime image".equals(baseline.category())
                && baseline.version() != null
                && baseline.version().toLowerCase().contains("11")) {
            reasons.add("Runtime image still references Java 11");
        }
    }

    private static int baselineRiskScore(List<DependencyBaseline> baselines, int targetJavaVersion) {
        var score = 0;
        for (DependencyBaseline baseline : baselines) {
            if ("Gradle wrapper".equals(baseline.name())
                    && targetJavaVersion >= 21
                    && parseMajor(baseline.version()).orElse(99) < 7) {
                score += 10;
            }
            if ("Runtime image".equals(baseline.category())
                    && baseline.version() != null
                    && baseline.version().toLowerCase().contains("11")) {
                score += 10;
            }
        }
        return score;
    }

    private static RiskLevel levelFor(int score) {
        if (score >= 60) {
            return RiskLevel.HIGH;
        }
        if (score >= 25) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static OptionalInt parseMajor(String value) {
        if (value == null || value.isBlank()) {
            return OptionalInt.empty();
        }
        var matcher = FIRST_INTEGER.matcher(value);
        return matcher.find() ? OptionalInt.of(Integer.parseInt(matcher.group())) : OptionalInt.empty();
    }

    private static java.util.Optional<MajorMinor> parseSpringBootMajorMinor(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        var parts = value.split("\\.");
        if (parts.length < 2) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(new MajorMinor(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }

    private record MajorMinor(int major, int minor) {
    }
}
