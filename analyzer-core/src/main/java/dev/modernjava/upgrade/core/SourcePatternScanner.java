package dev.modernjava.upgrade.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public final class SourcePatternScanner {

    public List<SourcePattern> scan(Path projectPath) {
        Objects.requireNonNull(projectPath, "projectPath");
        var root = projectPath.toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            return List.of();
        }

        try (var paths = Files.walk(root)) {
            var javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !isUnderIgnoredDirectory(root, path))
                    .sorted(Comparator.comparing(path -> root.relativize(path).toString()))
                    .toList();
            var patterns = new ArrayList<SourcePattern>();
            for (Path javaFile : javaFiles) {
                scanFile(root, javaFile, patterns);
            }
            return List.copyOf(patterns);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not scan Java source files under " + root, exception);
        }
    }

    private static void scanFile(Path root, Path javaFile, List<SourcePattern> patterns) throws IOException {
        var lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
        var relativePath = root.relativize(javaFile);
        var reportedTypes = EnumSet.noneOf(SourcePatternType.class);
        for (int index = 0; index < lines.size(); index++) {
            var line = stripLineComment(lines.get(index));
            if (line.stripLeading().startsWith("import ")) {
                continue;
            }
            if (line.contains("Map<String, Object>") && reportedTypes.add(SourcePatternType.MAP_STRING_OBJECT)) {
                patterns.add(new SourcePattern(
                        SourcePatternType.MAP_STRING_OBJECT,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (line.contains("SimpleDateFormat") && reportedTypes.add(SourcePatternType.SIMPLE_DATE_FORMAT)) {
                patterns.add(new SourcePattern(
                        SourcePatternType.SIMPLE_DATE_FORMAT,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if ((line.contains("Executors.newFixedThreadPool") || line.contains("Executors.newCachedThreadPool"))
                    && reportedTypes.add(SourcePatternType.EXECUTOR_FACTORY)) {
                patterns.add(new SourcePattern(
                        SourcePatternType.EXECUTOR_FACTORY,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
        }
    }

    private static boolean isUnderIgnoredDirectory(Path root, Path path) {
        var relative = root.relativize(path);
        for (Path part : relative) {
            if ("target".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static String stripLineComment(String line) {
        var commentStart = line.indexOf("//");
        return commentStart >= 0 ? line.substring(0, commentStart) : line;
    }
}
