package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SourcePatternScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsModernizationPatternsInJavaSourceFiles() throws Exception {
        var source = tempDir.resolve("src/main/java/example/LegacyController.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import java.text.SimpleDateFormat;
                import java.util.Map;
                import java.util.concurrent.Executors;

                class LegacyController {
                    Map<String, Object> response() {
                        var formatter = new SimpleDateFormat("yyyy-MM-dd");
                        var executor = Executors.newFixedThreadPool(4);
                        return Map.of();
                    }
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(
                        SourcePatternType.MAP_STRING_OBJECT,
                        SourcePatternType.SIMPLE_DATE_FORMAT,
                        SourcePatternType.EXECUTOR_FACTORY);
        assertThat(patterns)
                .extracting(SourcePattern::relativePath)
                .containsOnly(Path.of("src/main/java/example/LegacyController.java"));
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(8, 9, 10);
    }

    @Test
    void ignoresNonJavaFilesAndTargetDirectory() throws Exception {
        var ignoredJava = tempDir.resolve("target/generated/Generated.java");
        Files.createDirectories(ignoredJava.getParent());
        Files.writeString(ignoredJava, "class Generated { java.util.Map<String, Object> x; }");
        Files.writeString(tempDir.resolve("README.md"), "Map<String, Object>");

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns).isEmpty();
    }

    @Test
    void reportsOnlyFirstOccurrenceOfSamePatternPerFile() throws Exception {
        var source = tempDir.resolve("src/main/java/example/LegacyController.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import java.util.Map;

                class LegacyController {
                    Map<String, Object> response() {
                        Map<String, Object> body = Map.of();
                        return body;
                    }
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.MAP_STRING_OBJECT);
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(6);
    }
}
