package dev.modernjava.upgrade.core;

import java.nio.file.Path;

public record AnalysisRequest(Path projectPath, int targetJavaVersion) {
}
