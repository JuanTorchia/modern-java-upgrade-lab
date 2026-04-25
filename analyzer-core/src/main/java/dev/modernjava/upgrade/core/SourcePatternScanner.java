package dev.modernjava.upgrade.core;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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
import java.util.Optional;
import java.util.regex.Pattern;

public final class SourcePatternScanner {

    private static final Pattern UNSAFE_SIMPLE_NAME = Pattern.compile("\\bUnsafe\\b");
    private static final Pattern JAVA_EE_REMOVED_API = Pattern.compile(
            "\\b(?:javax\\.xml\\.bind|javax\\.activation|javax\\.annotation|javax\\.xml\\.ws|javax\\.jws)\\b");
    private static final Pattern JDK_INTERNAL_API = Pattern.compile("\\b(?:sun\\.misc|com\\.sun|jdk\\.internal)\\b");
    private static final Pattern REFLECTIVE_ACCESS = Pattern.compile(
            "\\b(?:setAccessible\\s*\\(\\s*true\\s*\\)|Class\\.forName\\s*\\()");
    private static final Pattern SECURITY_MANAGER_USAGE = Pattern.compile(
            "\\b(?:System\\.getSecurityManager\\s*\\(|SecurityManager\\b)");
    private static final Pattern FINALIZATION_USAGE = Pattern.compile("\\bfinalize\\s*\\(\\s*\\)");

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
        var seenTypes = EnumSet.noneOf(SourcePatternType.class);
        var filePatterns = new ArrayList<SourcePattern>();
        var importsUnsafe = importsSunMiscUnsafe(sanitizedLines);
        for (int index = 0; index < lines.size(); index++) {
            var line = sanitizedLines.get(index);
            if (JAVA_EE_REMOVED_API.matcher(line).find()
                    && seenTypes.add(SourcePatternType.JAVA_EE_REMOVED_API)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.JAVA_EE_REMOVED_API,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (line.stripLeading().startsWith("import ")) {
                continue;
            }
            if (line.contains("Map<String, Object>") && seenTypes.add(SourcePatternType.MAP_STRING_OBJECT)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.MAP_STRING_OBJECT,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (line.contains("SimpleDateFormat") && seenTypes.add(SourcePatternType.SIMPLE_DATE_FORMAT)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.SIMPLE_DATE_FORMAT,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if ((line.contains("Executors.newFixedThreadPool") || line.contains("Executors.newCachedThreadPool"))
                    && seenTypes.add(SourcePatternType.EXECUTOR_FACTORY)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.EXECUTOR_FACTORY,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (line.contains("ThreadLocal") && seenTypes.add(SourcePatternType.THREAD_LOCAL)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.THREAD_LOCAL,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (isUnsafeUsage(line, importsUnsafe) && seenTypes.add(SourcePatternType.UNSAFE_MEMORY_ACCESS)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.UNSAFE_MEMORY_ACCESS,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (JDK_INTERNAL_API.matcher(line).find() && seenTypes.add(SourcePatternType.JDK_INTERNAL_API)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.JDK_INTERNAL_API,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (REFLECTIVE_ACCESS.matcher(line).find() && seenTypes.add(SourcePatternType.REFLECTIVE_ACCESS)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.REFLECTIVE_ACCESS,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (SECURITY_MANAGER_USAGE.matcher(line).find()
                    && seenTypes.add(SourcePatternType.SECURITY_MANAGER_USAGE)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.SECURITY_MANAGER_USAGE,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
            if (FINALIZATION_USAGE.matcher(line).find() && seenTypes.add(SourcePatternType.FINALIZATION_USAGE)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.FINALIZATION_USAGE,
                        relativePath,
                        index + 1,
                        lines.get(index)));
            }
        }
        findStructuredConcurrencyPreviewLine(lines).ifPresent(lineNumber -> {
            if (seenTypes.add(SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW)) {
                filePatterns.add(new SourcePattern(
                        SourcePatternType.STRUCTURED_CONCURRENCY_PREVIEW,
                        relativePath,
                        lineNumber,
                        lines.get(lineNumber - 1)));
            }
        });
        filePatterns.sort(Comparator.comparingInt(SourcePattern::lineNumber));
        patterns.addAll(filePatterns);
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

    private static Optional<Integer> findStructuredConcurrencyPreviewLine(List<String> lines) {
        try {
            var compilationUnit = StaticJavaParser.parse(String.join(System.lineSeparator(), lines));
            return compilationUnit.getImports().stream()
                    .filter(importDeclaration -> referencesStructuredTaskScope(importDeclaration.getNameAsString()))
                    .findFirst()
                    .flatMap(SourcePatternScanner::lineNumber)
                    .or(() -> compilationUnit.findAll(ClassOrInterfaceType.class).stream()
                            .filter(type -> referencesStructuredTaskScope(type.asString()))
                            .findFirst()
                            .flatMap(SourcePatternScanner::lineNumber));
        } catch (ParseProblemException exception) {
            return Optional.empty();
        }
    }

    private static boolean referencesStructuredTaskScope(String name) {
        return name.equals("java.util.concurrent.StructuredTaskScope")
                || name.startsWith("java.util.concurrent.StructuredTaskScope.")
                || name.startsWith("java.util.concurrent.StructuredTaskScope<");
    }

    private static Optional<Integer> lineNumber(Node node) {
        return node.getRange().map(range -> range.begin.line);
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
