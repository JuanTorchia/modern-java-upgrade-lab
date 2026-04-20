package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.build.ProjectInspector;
import dev.modernjava.upgrade.core.AnalysisRequest;
import dev.modernjava.upgrade.core.DefaultAnalyzer;
import dev.modernjava.upgrade.core.MarkdownReportRenderer;
import dev.modernjava.upgrade.core.ProjectMetadata;
import dev.modernjava.upgrade.core.SourcePatternScanner;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report.")
public final class AnalyzeCommand implements Callable<Integer> {

    @Option(names = "--path", description = "Project path to analyze.", defaultValue = ".")
    private Path path;

    @Option(names = "--target", description = "Target Java version.", required = true)
    private int targetVersion;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        var request = new AnalysisRequest(path, targetVersion);
        ProjectMetadata inspectedMetadata = new ProjectInspector().inspect(path);
        var sourcePatterns = new SourcePatternScanner().scan(path);
        var metadata = new ProjectMetadata(
                inspectedMetadata.buildTool(),
                inspectedMetadata.declaredJavaVersion(),
                inspectedMetadata.springBootVersion(),
                inspectedMetadata.dependencies(),
                inspectedMetadata.buildPlugins(),
                sourcePatterns);
        var result = new DefaultAnalyzer(metadata).analyze(request);
        var markdown = new MarkdownReportRenderer().render(request, result);
        spec.commandLine().getOut().println(markdown);
        return 0;
    }
}
