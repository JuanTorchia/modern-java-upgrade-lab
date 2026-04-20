package dev.modernjava.upgrade.cli;

import java.nio.file.Path;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report.")
public class AnalyzeCommand implements Runnable {

    @Option(names = "--path", defaultValue = ".")
    private Path path;

    @Option(names = "--target", required = true)
    private int targetVersion;

    @Override
    public void run() {
        System.out.println("# Modern Java Upgrade Report");
        System.out.println();
        System.out.println("## Project Path");
        System.out.println("`" + path + "`");
        System.out.println();
        System.out.println("## Target Java Version");
        System.out.println(targetVersion);
        System.out.println();
        System.out.println("Analyzer wiring will be connected in the next task.");
    }
}
