package dev.modernjava.upgrade.core;

import java.util.Objects;

public record RuleContext(AnalysisRequest request, ProjectMetadata metadata) {

    public RuleContext {
        request = Objects.requireNonNull(request, "request");
        metadata = Objects.requireNonNull(metadata, "metadata");
    }
}
