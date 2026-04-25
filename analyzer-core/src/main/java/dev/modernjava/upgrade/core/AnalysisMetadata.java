package dev.modernjava.upgrade.core;

import java.time.Instant;

public record AnalysisMetadata(
        String analyzerVersion,
        Instant generatedAt,
        String gitCommit,
        String gitBranch) {

    public static AnalysisMetadata unknown() {
        return new AnalysisMetadata("unknown", null, null, null);
    }
}
