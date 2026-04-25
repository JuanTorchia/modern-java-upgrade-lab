package dev.modernjava.upgrade.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "mjul",
        mixinStandardHelpOptions = true,
        version = "mjul 0.1.0-SNAPSHOT",
        description = "Generate evidence-based Java LTS migration reports.",
        subcommands = {AnalyzeCommand.class, PortfolioCommand.class})
public final class ModernJavaUpgradeLabApp implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ModernJavaUpgradeLabApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }
}
