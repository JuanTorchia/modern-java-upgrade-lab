package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.build.MavenProjectInspector;
import dev.modernjava.upgrade.core.AnalysisRequest;
import dev.modernjava.upgrade.core.AnalysisResult;
import dev.modernjava.upgrade.core.Finding;
import dev.modernjava.upgrade.core.FindingSeverity;
import dev.modernjava.upgrade.core.MarkdownReportRenderer;
import dev.modernjava.upgrade.core.ProjectMetadata;
import dev.modernjava.upgrade.rewrite.OpenRewriteSuggestion;
import dev.modernjava.upgrade.rewrite.OpenRewriteSuggestionService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
        ProjectMetadata metadata = new MavenProjectInspector().inspect(path);
        List<Finding> findings = new ArrayList<>();

        var springBootVersion = metadata.springBootVersion();
        if (targetVersion >= 21 && springBootVersion != null && springBootVersion.startsWith("2.")) {
            findings.add(new Finding(
                    "spring-boot-2-java-21-risk",
                    FindingSeverity.RISK,
                    "Spring Boot compatibility",
                    "Spring Boot 2.x needs review before a Java " + targetVersion + " production migration",
                    "Detected Spring Boot " + springBootVersion,
                    "Plan a Spring Boot 3.x migration path and validate framework compatibility before relying on Java "
                            + targetVersion + ".",
                    "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2"));
        }

        List<OpenRewriteSuggestion> suggestions = new OpenRewriteSuggestionService().suggestForTarget(targetVersion);
        if (!suggestions.isEmpty()) {
            OpenRewriteSuggestion suggestion = suggestions.getFirst();
            findings.add(new Finding(
                    "openrewrite-java-" + targetVersion,
                    FindingSeverity.INFO,
                    "Migration automation",
                    "OpenRewrite has a Java " + targetVersion + " migration recipe",
                    "Target Java version is " + targetVersion,
                    "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                    suggestion.recipe()));
        }

        var markdown = new MarkdownReportRenderer().render(request, new AnalysisResult(metadata, targetVersion, findings));
        spec.commandLine().getOut().println(markdown);
        return 0;
    }
}
