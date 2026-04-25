package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record AnalysisResult(
        ProjectMetadata metadata,
        int targetJavaVersion,
        List<Finding> findings,
        AnalysisMetadata analysisMetadata,
        RiskAssessment riskAssessment,
        List<WorkItem> workItems) {

    public AnalysisResult {
        metadata = Objects.requireNonNull(metadata, "metadata");
        findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
        analysisMetadata = Objects.requireNonNull(analysisMetadata, "analysisMetadata");
        riskAssessment = Objects.requireNonNull(riskAssessment, "riskAssessment");
        workItems = List.copyOf(Objects.requireNonNull(workItems, "workItems"));
    }

    public AnalysisResult(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {
        this(metadata, targetJavaVersion, findings, AnalysisMetadata.unknown());
    }

    public AnalysisResult(
            ProjectMetadata metadata,
            int targetJavaVersion,
            List<Finding> findings,
            AnalysisMetadata analysisMetadata) {
        this(
                metadata,
                targetJavaVersion,
                findings,
                analysisMetadata,
                new RiskAssessor().assess(metadata, targetJavaVersion, findings),
                new WorkItemGenerator().generate(metadata, targetJavaVersion, findings));
    }
}
