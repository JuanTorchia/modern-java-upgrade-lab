package dev.modernjava.upgrade.core;

import java.util.List;

public record AnalysisResult(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {
}
