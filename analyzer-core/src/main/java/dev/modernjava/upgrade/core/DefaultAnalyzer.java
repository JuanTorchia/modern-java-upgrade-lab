package dev.modernjava.upgrade.core;

import java.util.Objects;

public final class DefaultAnalyzer implements Analyzer {

    private final ProjectMetadata metadata;
    private final RuleEngine ruleEngine;

    public DefaultAnalyzer(ProjectMetadata metadata) {
        this(metadata, new RuleEngine(DefaultMigrationRules.defaults()));
    }

    DefaultAnalyzer(ProjectMetadata metadata, RuleEngine ruleEngine) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.ruleEngine = Objects.requireNonNull(ruleEngine, "ruleEngine");
    }

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        var context = new RuleContext(request, metadata);
        return new AnalysisResult(metadata, request.targetJavaVersion(), ruleEngine.evaluate(context));
    }

    public AnalysisResult analyze(AnalysisRequest request, AnalysisMetadata analysisMetadata) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(analysisMetadata, "analysisMetadata");
        var context = new RuleContext(request, metadata);
        return new AnalysisResult(metadata, request.targetJavaVersion(), ruleEngine.evaluate(context), analysisMetadata);
    }
}
