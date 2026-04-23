package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Locale;

public final class DefaultMigrationRules {

    private static final String MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins:maven-compiler-plugin";

    private DefaultMigrationRules() {
    }

    public static List<MigrationRule> defaults() {
        return List.of(
                DefaultMigrationRules::java8Baseline,
                DefaultMigrationRules::java21To25BaselineReview,
                DefaultMigrationRules::springBoot2Compatibility,
                DefaultMigrationRules::springBootJava17To21BaselineReview,
                DefaultMigrationRules::explicitMavenCompilerPlugin,
                DefaultMigrationRules::java25PreviewFeatureBoundary,
                DefaultMigrationRules::openRewriteMigrationRecipe,
                DefaultMigrationRules::sourcePatternFindings);
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
        return context.metadata().sourcePatterns().stream()
                .filter(pattern -> shouldReportSourcePattern(context, pattern))
                .map(pattern -> switch (pattern.type()) {
                    case MAP_STRING_OBJECT -> mapStringObjectFinding(pattern);
                    case SIMPLE_DATE_FORMAT -> simpleDateFormatFinding(pattern);
                    case EXECUTOR_FACTORY -> executorFactoryFinding(pattern);
                    case THREAD_LOCAL -> threadLocalFinding(pattern);
                    case UNSAFE_MEMORY_ACCESS -> unsafeMemoryAccessFinding(pattern);
                })
                .toList();
    }

    private static boolean shouldReportSourcePattern(RuleContext context, SourcePattern pattern) {
        return switch (pattern.type()) {
            case MAP_STRING_OBJECT, SIMPLE_DATE_FORMAT -> targetsJava17OrLater(context);
            case EXECUTOR_FACTORY -> context.request().targetJavaVersion() >= 21;
            case THREAD_LOCAL -> declaresJava21(context.metadata()) && context.request().targetJavaVersion() == 25;
            case UNSAFE_MEMORY_ACCESS -> declaresJava21(context.metadata()) && context.request().targetJavaVersion() == 25;
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

    private static String sourceEvidence(SourcePattern pattern) {
        return pattern.relativePath() + ":" + pattern.lineNumber() + " contains `" + pattern.evidence() + "`";
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

    private static boolean declaresJava8(ProjectMetadata metadata) {
        return "8".equals(metadata.declaredJavaVersion()) || "1.8".equals(metadata.declaredJavaVersion());
    }

    private static boolean declaresJava17(ProjectMetadata metadata) {
        return "17".equals(metadata.declaredJavaVersion());
    }

    private static boolean declaresJava21(ProjectMetadata metadata) {
        return "21".equals(metadata.declaredJavaVersion());
    }
}
