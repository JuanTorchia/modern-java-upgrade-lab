package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record AnalysisResult(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {

    public AnalysisResult {
        metadata = Objects.requireNonNull(metadata, "metadata");
        findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
    }
}
