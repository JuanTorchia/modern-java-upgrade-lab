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

    @Test
    void detectsThreadLocalUsageOutsideImports() throws Exception {
        var source = tempDir.resolve("src/main/java/example/RequestContext.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import java.lang.ThreadLocal;

                final class RequestContext {
                    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.THREAD_LOCAL);
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(6);
    }

    @Test
    void detectsDirectUnsafeUsageOutsideImports() throws Exception {
        var source = tempDir.resolve("src/main/java/example/UnsafeHolder.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import sun.misc.Unsafe;

                final class UnsafeHolder {
                    private static final Unsafe UNSAFE = lookupUnsafe();
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.UNSAFE_MEMORY_ACCESS);
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(6);
    }

    @Test
    void detectsStructuredConcurrencyPreviewImport() throws Exception {
        var source = tempDir.resolve("src/main/java/example/StructuredWorker.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import java.util.concurrent.StructuredTaskScope;

                final class StructuredWorker {
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW);
        assertThat(patterns)
                .extracting(SourcePattern::relativePath)
                .containsExactly(Path.of("src/main/java/example/StructuredWorker.java"));
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(3);
    }

    @Test
    void keepsStructuredConcurrencyPreviewInSourceOrder() throws Exception {
        var source = tempDir.resolve("src/main/java/example/StructuredController.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import java.util.concurrent.StructuredTaskScope;
                import java.util.Map;

                final class StructuredController {
                    Map<String, Object> response() {
                        return Map.of();
                    }
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(
                        SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW,
                        SourcePatternType.MAP_STRING_OBJECT);
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(3, 7);
    }

    @Test
    void detectsStructuredConcurrencyPreviewQualifiedReference() throws Exception {
        var source = tempDir.resolve("src/main/java/example/StructuredWorker.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                final class StructuredWorker {
                    java.util.concurrent.StructuredTaskScope<?> scope;
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW);
        assertThat(patterns)
                .extracting(SourcePattern::lineNumber)
                .containsExactly(4);
    }

    @Test
    void ignoresStructuredConcurrencyTextInCommentsStringsAndUnrelatedIdentifiers() throws Exception {
        var source = tempDir.resolve("src/main/java/example/StructuredText.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                final class StructuredText {
                    String text = "java.util.concurrent.StructuredTaskScope";
                    // import java.util.concurrent.StructuredTaskScope;
                    Object StructuredTaskScope = new Object();
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns).isEmpty();
    }

    @Test
    void skipsMalformedJavaFilesDuringStructuredConcurrencyParsing() throws Exception {
        var malformed = tempDir.resolve("src/main/java/example/Broken.java");
        Files.createDirectories(malformed.getParent());
        Files.writeString(malformed, """
                package example;

                class Broken {
                    void broken(
                }
                """);
        var valid = tempDir.resolve("src/main/java/example/Valid.java");
        Files.writeString(valid, """
                package example;

                import java.lang.ThreadLocal;

                class Valid {
                    ThreadLocal<String> context = new ThreadLocal<>();
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.THREAD_LOCAL);
    }

    @Test
    void ignoresUnsafeImportsWithoutActionableUsage() throws Exception {
        var source = tempDir.resolve("src/main/java/example/UnsafeImportOnly.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import sun.misc.Unsafe;

                final class UnsafeImportOnly {
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns).isEmpty();
    }

    @Test
    void ignoresUnsafeTextInStringsAndBlockComments() throws Exception {
        var source = tempDir.resolve("src/main/java/example/UnsafeText.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                final class UnsafeText {
                    String text = "sun.misc.Unsafe";
                    /*
                     * sun.misc.Unsafe unsafe;
                     */
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns).isEmpty();
    }

    @Test
    void ignoresUnsafeTextInTextBlocks() throws Exception {
        var source = tempDir.resolve("src/main/java/example/UnsafeTextBlock.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                final class UnsafeTextBlock {
                    String text = \"""
                            sun.misc.Unsafe
                            \""";
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns).isEmpty();
    }

    @Test
    void detectsJava8To11ReadinessPatterns() throws Exception {
        var source = tempDir.resolve("src/main/java/example/LegacyJava8.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                import javax.xml.bind.JAXBContext;

                final class LegacyJava8 {
                    void reflect(java.lang.reflect.Field field) throws Exception {
                        field.setAccessible(true);
                        System.getSecurityManager();
                    }

                    @Override
                    protected void finalize() {
                    }
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .contains(
                        SourcePatternType.JAVA_EE_REMOVED_API,
                        SourcePatternType.REFLECTIVE_ACCESS,
                        SourcePatternType.SECURITY_MANAGER_USAGE,
                        SourcePatternType.FINALIZATION_USAGE);
    }

    @Test
    void detectsJdkInternalApiUsageOutsideImports() throws Exception {
        var source = tempDir.resolve("src/main/java/example/InternalAccess.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                final class InternalAccess {
                    Object access = sun.misc.SharedSecrets.getJavaLangAccess();
                }
                """);

        var patterns = new SourcePatternScanner().scan(tempDir);

        assertThat(patterns)
                .extracting(SourcePattern::type)
                .containsExactly(SourcePatternType.JDK_INTERNAL_API);
    }
}
