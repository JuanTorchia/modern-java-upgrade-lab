package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.core.AnalysisMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

final class AnalysisMetadataCollector {

    private final Clock clock;

    AnalysisMetadataCollector() {
        this(Clock.systemUTC());
    }

    AnalysisMetadataCollector(Clock clock) {
        this.clock = clock;
    }

    AnalysisMetadata collect(Path projectPath) {
        return new AnalysisMetadata(
                analyzerVersion(),
                clock.instant(),
                gitValue(projectPath, "rev-parse", "HEAD").orElse(null),
                gitValue(projectPath, "branch", "--show-current").orElse(null));
    }

    private static String analyzerVersion() {
        var version = AnalyzeCommand.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "0.1.0-SNAPSHOT" : version;
    }

    private static Optional<String> gitValue(Path projectPath, String... args) {
        var command = new java.util.ArrayList<String>();
        command.add("git");
        command.add("-C");
        command.add(projectPath.toAbsolutePath().normalize().toString());
        command.addAll(List.of(args));

        try {
            var process = new ProcessBuilder(command)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (process.waitFor() != 0 || output.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(output);
        } catch (IOException exception) {
            return Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
