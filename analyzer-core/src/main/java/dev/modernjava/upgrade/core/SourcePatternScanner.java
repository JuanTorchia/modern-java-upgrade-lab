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
import java.util.regex.Pattern;

public final class SourcePatternScanner {

    private static final Pattern UNSAFE_SIMPLE_NAME = Pattern.compile("\\bUnsafe\\b");

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
        var sanitizedLines = stripCommentsAndLiterals(lines);
        var relativePath = root.relativize(javaFile);
        var reportedTypes = EnumSet.noneOf(SourcePatternType.class);
        var importsUnsafe = importsSunMiscUnsafe(sanitizedLines);
        for (int index = 0; index < lines.size(); index++) {
            var line = sanitizedLines.get(index);
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
            if (line.contains("ThreadLocal") && reportedTypes.add(SourcePatternType.THREAD_LOCAL)) {
                patterns.add(new SourcePattern(
                        SourcePatternType.THREAD_LOCAL,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (isUnsafeUsage(line, importsUnsafe) && reportedTypes.add(SourcePatternType.UNSAFE_MEMORY_ACCESS)) {
                patterns.add(new SourcePattern(
                        SourcePatternType.UNSAFE_MEMORY_ACCESS,
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

    private static boolean importsSunMiscUnsafe(List<String> lines) {
        return lines.stream()
                .anyMatch(line -> line.stripLeading().matches("import\\s+sun\\.misc\\.Unsafe\\s*;.*"));
    }

    private static boolean isUnsafeUsage(String line, boolean importsUnsafe) {
        return line.contains("sun.misc.Unsafe") || (importsUnsafe && UNSAFE_SIMPLE_NAME.matcher(line).find());
    }

    private static List<String> stripCommentsAndLiterals(List<String> lines) {
        var sanitizedLines = new ArrayList<String>(lines.size());
        var inBlockComment = false;
        var inTextBlock = false;
        for (String line : lines) {
            var sanitized = new StringBuilder();
            for (int index = 0; index < line.length();) {
                if (inTextBlock) {
                    var textBlockEnd = line.indexOf("\"\"\"", index);
                    if (textBlockEnd < 0) {
                        break;
                    }
                    inTextBlock = false;
                    index = textBlockEnd + 3;
                    continue;
                }

                if (inBlockComment) {
                    if (startsWith(line, index, "*/")) {
                        inBlockComment = false;
                        index += 2;
                    } else {
                        index++;
                    }
                    continue;
                }

                if (startsWith(line, index, "//")) {
                    break;
                }
                if (startsWith(line, index, "/*")) {
                    inBlockComment = true;
                    index += 2;
                    continue;
                }
                if (startsWith(line, index, "\"\"\"")) {
                    inTextBlock = true;
                    index += 3;
                    continue;
                }

                var current = line.charAt(index);
                if (current == '"' || current == '\'') {
                    sanitized.append(' ');
                    index = skipQuotedLiteral(line, index, current);
                    continue;
                }

                sanitized.append(current);
                index++;
            }
            sanitizedLines.add(sanitized.toString());
        }
        return List.copyOf(sanitizedLines);
    }

    private static int skipQuotedLiteral(String line, int startIndex, char quote) {
        var index = startIndex + 1;
        while (index < line.length()) {
            var current = line.charAt(index);
            if (current == '\\') {
                index += 2;
            } else if (current == quote) {
                return index + 1;
            } else {
                index++;
            }
        }
        return index;
    }

    private static boolean startsWith(String line, int index, String token) {
        return index + token.length() <= line.length() && line.startsWith(token, index);
    }
}
