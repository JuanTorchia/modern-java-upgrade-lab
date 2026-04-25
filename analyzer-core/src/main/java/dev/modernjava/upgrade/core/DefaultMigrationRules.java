package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class DefaultMigrationRules {

    private static final String MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins:maven-compiler-plugin";

    private DefaultMigrationRules() {
    }

    public static List<MigrationRule> defaults() {
        return List.of(
                DefaultMigrationRules::java8To11Baseline,
                DefaultMigrationRules::java8Baseline,
                DefaultMigrationRules::java21To25BaselineReview,
                DefaultMigrationRules::springBoot2Compatibility,
                DefaultMigrationRules::springBootJava17To21BaselineReview,
                DefaultMigrationRules::legacyTestPluginBaseline,
                DefaultMigrationRules::legacyDependencyBaseline,
                DefaultMigrationRules::runtimeBaseline,
                DefaultMigrationRules::explicitMavenCompilerPlugin,
                DefaultMigrationRules::java25PreviewFeatureBoundary,
                DefaultMigrationRules::openRewriteMigrationRecipe,
                DefaultMigrationRules::sourcePatternFindings);
    }

    private static List<Finding> java8To11Baseline(RuleContext context) {
        if (!isJava8To11(context)) {
            return List.of();
        }

        return List.of(new Finding(
                "java-8-to-11-baseline",
                FindingCategory.BASELINE,
                FindingSeverity.INFO,
                "Java baseline",
                "Java 8 to 11 migration needs a compatibility baseline",
                "Declared Java version is " + context.metadata().declaredJavaVersion()
                        + "; target Java version is " + context.request().targetJavaVersion(),
                "Run the full test suite on Java 8, then establish a separate Java 11 branch that validates removed Java EE modules, reflection warnings, build plugins, and runtime images.",
                null));
    }

    private static List<Finding> java8Baseline(RuleContext context) {
        if (!targetsJava17OrLater(context) || !declaresJava8(context.metadata())) {
            return List.of();
        }

        return List.of(new Finding(
                "java-8-baseline-target-" + context.request().targetJavaVersion(),
                FindingCategory.BASELINE,
                FindingSeverity.INFO,
                "Java baseline",
                "Java 8 baseline should be migrated deliberately before adopting Java "
                        + context.request().targetJavaVersion(),
                "Declared Java version is " + context.metadata().declaredJavaVersion(),
                "Establish a Java " + context.request().targetJavaVersion()
                        + " build and test baseline before introducing optional language modernization.",
                null));
    }

    private static List<Finding> java21To25BaselineReview(RuleContext context) {
        if (context.request().targetJavaVersion() != 25 || !declaresJava21(context.metadata())) {
            return List.of();
        }

        return List.of(new Finding(
                "java-21-to-25-baseline-review",
                FindingCategory.BASELINE,
                FindingSeverity.INFO,
                "Java baseline",
                "Java 21 to 25 migration should start with a build and test baseline",
                "Declared Java version is " + context.metadata().declaredJavaVersion()
                        + "; target Java version is " + context.request().targetJavaVersion(),
                "Establish a Java 25 build and test baseline before enabling optional language, runtime, GC, JFR, or AOT changes.",
                null));
    }

    private static List<Finding> springBoot2Compatibility(RuleContext context) {
        var springBootVersion = context.metadata().springBootVersion();
        if (!targetsJava17OrLater(context) || springBootVersion == null || !springBootVersion.startsWith("2.")) {
            return List.of();
        }

        return List.of(new Finding(
                "spring-boot-2-java-" + context.request().targetJavaVersion() + "-risk",
                FindingCategory.FRAMEWORK,
                FindingSeverity.RISK,
                "Spring Boot compatibility",
                "Spring Boot 2.x needs compatibility validation before a Java "
                        + context.request().targetJavaVersion() + " migration",
                "Detected Spring Boot " + springBootVersion,
                "Validate the project on Spring Boot 2.7.x before the Java "
                        + context.request().targetJavaVersion()
                        + " rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.",
                null));
    }

    private static List<Finding> springBootJava17To21BaselineReview(RuleContext context) {
        var springBootVersion = context.metadata().springBootVersion();
        if (context.request().targetJavaVersion() != 21
                || !declaresJava17(context.metadata())
                || springBootVersion == null
                || springBootVersion.startsWith("2.")) {
            return List.of();
        }

        return List.of(new Finding(
                "spring-boot-java-17-to-21-baseline-review",
                FindingCategory.FRAMEWORK,
                FindingSeverity.INFO,
                "Spring Boot compatibility",
                "Spring Boot baseline should be reviewed before a Java 21 rollout",
                "Declared Java version is " + context.metadata().declaredJavaVersion()
                        + "; detected Spring Boot " + springBootVersion
                        + "; target Java version is " + context.request().targetJavaVersion(),
                "Validate the selected Spring Boot line against Java 21 before runtime rollout. Treat framework upgrades, dependency baselines, and CI runtime changes as explicit migration work rather than language modernization.",
                null));
    }

    private static List<Finding> explicitMavenCompilerPlugin(RuleContext context) {
        if (!targetsJava17OrLater(context) || !"maven".equalsIgnoreCase(context.metadata().buildTool())) {
            return List.of();
        }
        if (context.metadata().buildPlugins().contains(MAVEN_COMPILER_PLUGIN)) {
            return List.of();
        }

        return List.of(new Finding(
                "maven-compiler-plugin-explicit-config",
                FindingCategory.BUILD,
                FindingSeverity.INFO,
                "Build configuration",
                "Maven compiler configuration should be explicit for Java "
                        + context.request().targetJavaVersion() + " migration evidence",
                "No maven-compiler-plugin entry was detected in build plugins",
                "Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.",
                null));
    }

    private static List<Finding> legacyTestPluginBaseline(RuleContext context) {
        if (context.request().targetJavaVersion() < 11) {
            return List.of();
        }

        return context.metadata().dependencyBaselines().stream()
                .filter(baseline -> isTestPlugin(baseline.name()))
                .filter(baseline -> isOlderThanMajor(baseline.version(), 3))
                .map(baseline -> new Finding(
                        "java-8-to-11-test-plugin-baseline-" + findingToken(baseline.name()),
                        FindingCategory.BUILD,
                        FindingSeverity.RISK,
                        "Test runtime",
                        displayArtifact(baseline.name()) + " should be upgraded before Java "
                                + context.request().targetJavaVersion() + " test baselining",
                        "Detected " + baseline.name() + " " + baseline.version() + " in " + baseline.evidence(),
                        "Upgrade Surefire/Failsafe to a Java "
                                + context.request().targetJavaVersion()
                                + " compatible baseline before trusting migration test results.",
                        null))
                .toList();
    }

    private static List<Finding> legacyDependencyBaseline(RuleContext context) {
        if (context.request().targetJavaVersion() < 17) {
            return List.of();
        }

        return context.metadata().dependencyBaselines().stream()
                .filter(DefaultMigrationRules::isKnownBytecodeSensitiveDependency)
                .filter(baseline -> isOlderThanMajor(baseline.version(), minimumMajorFor(baseline.name())))
                .map(baseline -> new Finding(
                        "legacy-dependency-" + findingToken(baseline.name()),
                        FindingCategory.BUILD,
                        FindingSeverity.RISK,
                        "Dependency compatibility",
                        displayArtifact(baseline.name()) + " baseline should be reviewed before Java "
                                + context.request().targetJavaVersion(),
                        "Detected " + baseline.name() + " " + baseline.version() + " in " + baseline.evidence(),
                        "Validate this dependency against the target JDK and upgrade it in a dedicated build-readiness branch.",
                        null))
                .toList();
    }

    private static List<Finding> runtimeBaseline(RuleContext context) {
        return context.metadata().dependencyBaselines().stream()
                .filter(baseline -> "Runtime image".equals(baseline.category()))
                .filter(baseline -> imageMajor(baseline.version()) > 0
                        && imageMajor(baseline.version()) != context.request().targetJavaVersion())
                .map(baseline -> new Finding(
                        "runtime-image-target-mismatch",
                        FindingCategory.BUILD,
                        FindingSeverity.RISK,
                        "Runtime image",
                        "Runtime image should match the requested Java " + context.request().targetJavaVersion()
                                + " rollout",
                        "Detected runtime image " + baseline.version() + " in " + baseline.evidence(),
                        "Update container/runtime images separately from source changes and verify rollback to the previous runtime.",
                        null))
                .toList();
    }

    private static List<Finding> java25PreviewFeatureBoundary(RuleContext context) {
        if (context.request().targetJavaVersion() != 25 || !declaresJava21(context.metadata())) {
            return List.of();
        }

        var previewArg = context.metadata().compilerArgs().stream()
                .filter("--enable-preview"::equals)
                .findFirst()
                .orElse(null);
        if (previewArg == null) {
            return List.of();
        }

        return List.of(new Finding(
                "jdk-25-preview-feature-boundary",
                FindingCategory.BUILD,
                FindingSeverity.RISK,
                "Compiler flags",
                "Preview feature usage is a Java 25 migration boundary",
                "Declared Java version is " + context.metadata().declaredJavaVersion()
                        + "; target Java version is " + context.request().targetJavaVersion()
                        + "; detected compiler argument `" + previewArg + "`",
                "Treat preview/incubator feature usage as explicit technical debt. Verify source/bytecode compatibility on every JDK update.",
                null));
    }

    private static List<Finding> openRewriteMigrationRecipe(RuleContext context) {
        var recipe = switch (context.request().targetJavaVersion()) {
            case 17 -> "org.openrewrite.java.migrate.UpgradeToJava17";
            case 21 -> "org.openrewrite.java.migrate.UpgradeToJava21";
            case 25 -> "org.openrewrite.java.migrate.UpgradeToJava25";
            default -> null;
        };
        if (recipe == null) {
            return List.of();
        }

        return List.of(new Finding(
                "openrewrite-java-" + context.request().targetJavaVersion(),
                FindingCategory.AUTOMATION,
                FindingSeverity.INFO,
                "Migration automation",
                "OpenRewrite has a Java " + context.request().targetJavaVersion() + " migration recipe",
                "Target Java version is " + context.request().targetJavaVersion(),
                "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                recipe));
    }

    private static List<Finding> sourcePatternFindings(RuleContext context) {
        var reportablePatterns = context.metadata().sourcePatterns().stream()
                .filter(pattern -> shouldReportSourcePattern(context, pattern))
                .toList();
        var mapPatterns = reportablePatterns.stream()
                .filter(pattern -> pattern.type() == SourcePatternType.MAP_STRING_OBJECT)
                .toList();
        var nonMapFindings = reportablePatterns.stream()
                .filter(pattern -> pattern.type() != SourcePatternType.MAP_STRING_OBJECT)
                .map(pattern -> switch (pattern.type()) {
                    case MAP_STRING_OBJECT -> throw new IllegalStateException("Map patterns are handled separately");
                    case SIMPLE_DATE_FORMAT -> simpleDateFormatFinding(pattern);
                    case EXECUTOR_FACTORY -> executorFactoryFinding(pattern);
                    case THREAD_LOCAL -> threadLocalFinding(pattern);
                    case STRUCTURED_CONCURRENCY_PREVIEW -> structuredConcurrencyPreviewFinding(pattern);
                    case UNSAFE_MEMORY_ACCESS -> unsafeMemoryAccessFinding(pattern);
                    case JAVA_EE_REMOVED_API -> javaEeRemovedApiFinding(pattern);
                    case JDK_INTERNAL_API -> jdkInternalApiFinding(pattern);
                    case REFLECTIVE_ACCESS -> reflectiveAccessFinding(pattern);
                    case SECURITY_MANAGER_USAGE -> securityManagerUsageFinding(pattern);
                    case FINALIZATION_USAGE -> finalizationUsageFinding(pattern);
                });

        return Stream.concat(mapStringObjectFindings(mapPatterns).stream(), nonMapFindings)
                .toList();
    }

    private static boolean shouldReportSourcePattern(RuleContext context, SourcePattern pattern) {
        return switch (pattern.type()) {
            case MAP_STRING_OBJECT, SIMPLE_DATE_FORMAT -> targetsJava17OrLater(context);
            case EXECUTOR_FACTORY -> context.request().targetJavaVersion() >= 21;
            case THREAD_LOCAL -> declaresJava21(context.metadata()) && context.request().targetJavaVersion() == 25;
            case STRUCTURED_CONCURRENCY_PREVIEW -> declaresJava21(context.metadata()) && context.request().targetJavaVersion() == 25;
            case UNSAFE_MEMORY_ACCESS -> declaresJava21(context.metadata()) && context.request().targetJavaVersion() == 25;
            case JAVA_EE_REMOVED_API, JDK_INTERNAL_API, REFLECTIVE_ACCESS, SECURITY_MANAGER_USAGE,
                    FINALIZATION_USAGE -> isJava8To11(context) || targetsJava17OrLater(context);
        };
    }

    private static Finding mapStringObjectFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.LANGUAGE,
                FindingSeverity.INFO,
                "Language modernization",
                "Map-based response can be reviewed as an explicit DTO or record",
                sourceEvidence(pattern),
                "Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.",
                null);
    }

    private static List<Finding> mapStringObjectFindings(List<SourcePattern> patterns) {
        if (patterns.size() <= 3) {
            return patterns.stream()
                    .map(DefaultMigrationRules::mapStringObjectFinding)
                    .toList();
        }

        var examples = patterns.stream()
                .limit(3)
                .map(DefaultMigrationRules::sourceLocation)
                .toList();
        return List.of(new Finding(
                "source-map-string-object-summary",
                FindingCategory.LANGUAGE,
                FindingSeverity.INFO,
                "Language modernization",
                "Map-based responses can be reviewed as explicit DTOs or records",
                "Detected " + patterns.size() + " occurrences; examples: " + String.join("; ", examples),
                "Review the repeated response-shape pattern. Start by modeling the most stable API responses as DTOs and consider records after the migration baseline is stable.",
                null));
    }

    private static Finding simpleDateFormatFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.LANGUAGE,
                FindingSeverity.INFO,
                "Date and time API",
                "SimpleDateFormat usage can be reviewed for java.time migration",
                sourceEvidence(pattern),
                "Prefer java.time formatters for immutable, thread-safe date and time handling when modernizing the code path.",
                null);
    }

    private static Finding executorFactoryFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.CONCURRENCY,
                FindingSeverity.INFO,
                "Concurrency modernization",
                "Executor factory usage should be reviewed before adopting virtual threads",
                sourceEvidence(pattern),
                "Evaluate whether this blocking workload can use virtual threads after measuring behavior and preserving executor lifecycle boundaries.",
                null);
    }

    private static Finding threadLocalFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.CONCURRENCY,
                FindingSeverity.INFO,
                "Concurrency modernization",
                "ThreadLocal usage should be reviewed for scoped values",
                sourceEvidence(pattern),
                "Review whether this context propagation can move toward scoped values on Java 25. Do not rewrite automatically; validate lifecycle, framework integration, and request boundaries first.",
                null);
    }

    private static Finding structuredConcurrencyPreviewFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.CONCURRENCY,
                FindingSeverity.RISK,
                "Structured concurrency",
                "Structured concurrency preview usage is a Java 25 migration boundary",
                sourceEvidence(pattern),
                "Keep structured concurrency behind explicit preview boundaries. Do not present it as stable migration work; review source and bytecode compatibility on every JDK update.",
                null);
    }

    private static Finding unsafeMemoryAccessFinding(SourcePattern pattern) {
        return new Finding(
                sourceFindingId(pattern),
                FindingCategory.BUILD,
                FindingSeverity.RISK,
                "Unsafe memory access",
                "Direct sun.misc.Unsafe usage should be removed or isolated",
                sourceEvidence(pattern),
                "Remove direct unsafe memory-access usage where possible and audit dependencies that emit JDK warnings.",
                null);
    }

    private static Finding javaEeRemovedApiFinding(SourcePattern pattern) {
        return new Finding(
                "java-8-to-11-removed-java-ee-api",
                FindingCategory.BUILD,
                FindingSeverity.RISK,
                "Removed Java EE modules",
                "Removed Java EE/JAXB API usage blocks a clean Java 11 migration",
                sourceEvidence(pattern),
                "Add explicit dependencies or migrate the affected API before moving the runtime to Java 11.",
                null);
    }

    private static Finding jdkInternalApiFinding(SourcePattern pattern) {
        return new Finding(
                "java-8-to-11-jdk-internal-api",
                FindingCategory.BUILD,
                FindingSeverity.RISK,
                "JDK internals",
                "JDK internal API usage should be removed before Java 11+ migration",
                sourceEvidence(pattern),
                "Replace internal JDK API usage with supported APIs or isolate the dependency that requires it.",
                null);
    }

    private static Finding reflectiveAccessFinding(SourcePattern pattern) {
        return new Finding(
                "java-8-to-11-reflective-access",
                FindingCategory.BUILD,
                FindingSeverity.RISK,
                "Illegal reflective access",
                "Reflective access should be reviewed before Java 11+ migration",
                sourceEvidence(pattern),
                "Run tests on Java 11 with warnings enabled and remove or explicitly contain illegal reflective access.",
                null);
    }

    private static Finding securityManagerUsageFinding(SourcePattern pattern) {
        return new Finding(
                "java-8-to-11-security-manager-usage",
                FindingCategory.BUILD,
                FindingSeverity.INFO,
                "Security Manager",
                "SecurityManager usage should be reviewed during Java migration planning",
                sourceEvidence(pattern),
                "Document whether this code path is still active and plan removal before newer JDK rollouts.",
                null);
    }

    private static Finding finalizationUsageFinding(SourcePattern pattern) {
        return new Finding(
                "java-8-to-11-finalization-usage",
                FindingCategory.LANGUAGE,
                FindingSeverity.INFO,
                "Finalization",
                "finalize() usage should be removed during migration hardening",
                sourceEvidence(pattern),
                "Replace finalization with explicit lifecycle management or Cleaner after the runtime baseline is stable.",
                null);
    }

    private static String sourceEvidence(SourcePattern pattern) {
        return pattern.relativePath() + ":" + pattern.lineNumber() + " contains `" + pattern.evidence() + "`";
    }

    private static String sourceLocation(SourcePattern pattern) {
        return pattern.relativePath() + ":" + pattern.lineNumber();
    }

    private static String sourceFindingId(SourcePattern pattern) {
        var normalizedPath = pattern.relativePath().toString()
                .replace('\\', '-')
                .replace('/', '-')
                .replaceAll("[^A-Za-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase(Locale.ROOT);
        return "source-" + pattern.type().name().toLowerCase(Locale.ROOT).replace('_', '-')
                + "-" + normalizedPath + "-" + pattern.lineNumber();
    }

    private static boolean targetsJava17OrLater(RuleContext context) {
        return context.request().targetJavaVersion() >= 17;
    }

    private static boolean isJava8To11(RuleContext context) {
        return context.request().targetJavaVersion() == 11 && declaresJava8(context.metadata());
    }

    private static boolean declaresJava8(ProjectMetadata metadata) {
        return "8".equals(metadata.declaredJavaVersion()) || "1.8".equals(metadata.declaredJavaVersion());
    }

    private static boolean declaresJava17(ProjectMetadata metadata) {
        return "17".equals(metadata.declaredJavaVersion());
    }

    private static boolean declaresJava21(ProjectMetadata metadata) {
        return "21".equals(metadata.declaredJavaVersion());
    }

    private static boolean isTestPlugin(String name) {
        return "org.apache.maven.plugins:maven-surefire-plugin".equals(name)
                || "org.apache.maven.plugins:maven-failsafe-plugin".equals(name);
    }

    private static boolean isKnownBytecodeSensitiveDependency(DependencyBaseline baseline) {
        return "org.projectlombok:lombok".equals(baseline.name())
                || "org.mockito:mockito-core".equals(baseline.name())
                || "org.mockito:mockito-inline".equals(baseline.name())
                || "net.bytebuddy:byte-buddy".equals(baseline.name());
    }

    private static int minimumMajorFor(String artifact) {
        if (artifact.startsWith("org.mockito:")) {
            return 4;
        }
        return 1;
    }

    private static boolean isOlderThanMajor(String version, int major) {
        var parsed = firstVersionNumber(version);
        return parsed >= 0 && parsed < major;
    }

    private static int imageMajor(String value) {
        if (value == null) {
            return -1;
        }
        for (int version : List.of(25, 21, 17, 11, 8)) {
            if (value.contains(String.valueOf(version))) {
                return version;
            }
        }
        return -1;
    }

    private static int firstVersionNumber(String version) {
        if (version == null || version.isBlank()) {
            return -1;
        }
        var matcher = java.util.regex.Pattern.compile("\\d+").matcher(version);
        return matcher.find() ? Integer.parseInt(matcher.group()) : -1;
    }

    private static String findingToken(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private static String displayArtifact(String artifact) {
        var index = artifact.lastIndexOf(':');
        return index >= 0 ? artifact.substring(index + 1) : artifact;
    }
}
