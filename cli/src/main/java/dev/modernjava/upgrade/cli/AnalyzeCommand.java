package dev.modernjava.upgrade.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report.")
public final class AnalyzeCommand implements Callable<Integer> {

    @Option(names = "--path", description = "Project path to analyze.", defaultValue = ".")
    private Path path;

    @Option(names = "--target", description = "Target Java version.", required = true)
    private int targetVersion;

    @Override
    public Integer call() {
        System.out.println("# Modern Java Upgrade Report");
        System.out.println();
        System.out.println("## Project Path");
        System.out.println("`" + path + "`");
        System.out.println();
        System.out.println("## Target Java Version");
        System.out.println(targetVersion);
        System.out.println();
        System.out.println("Analyzer wiring will be connected in the next task.");
        return 0;
    }
}
