package dev.modernjava.upgrade.core;

import java.util.List;

public final class DefaultMigrationRules {

    private static final String MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins:maven-compiler-plugin";

    private DefaultMigrationRules() {
    }

    public static List<MigrationRule> defaults() {
        return List.of(
                DefaultMigrationRules::java8Baseline,
                DefaultMigrationRules::springBoot2Compatibility,
                DefaultMigrationRules::explicitMavenCompilerPlugin,
                DefaultMigrationRules::openRewriteMigrationRecipe);
    }

    private static List<Finding> java8Baseline(RuleContext context) {
        if (!targetsJava17OrLater(context) || !declaresJava8(context.metadata())) {
            return List.of();
        }

        return List.of(new Finding(
                "java-8-baseline-target-" + context.request().targetJavaVersion(),
                FindingSeverity.INFO,
                "Java baseline",
                "Java 8 baseline should be migrated deliberately before adopting Java "
                        + context.request().targetJavaVersion(),
                "Declared Java version is " + context.metadata().declaredJavaVersion(),
                "Establish a Java " + context.request().targetJavaVersion()
                        + " build and test baseline before introducing optional language modernization.",
                null));
    }

    private static List<Finding> springBoot2Compatibility(RuleContext context) {
        var springBootVersion = context.metadata().springBootVersion();
        if (!targetsJava17OrLater(context) || springBootVersion == null || !springBootVersion.startsWith("2.")) {
            return List.of();
        }

        return List.of(new Finding(
                "spring-boot-2-java-" + context.request().targetJavaVersion() + "-risk",
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

    private static List<Finding> explicitMavenCompilerPlugin(RuleContext context) {
        if (!targetsJava17OrLater(context) || !"maven".equalsIgnoreCase(context.metadata().buildTool())) {
            return List.of();
        }
        if (context.metadata().buildPlugins().contains(MAVEN_COMPILER_PLUGIN)) {
            return List.of();
        }

        return List.of(new Finding(
                "maven-compiler-plugin-explicit-config",
                FindingSeverity.INFO,
                "Build configuration",
                "Maven compiler configuration should be explicit for Java "
                        + context.request().targetJavaVersion() + " migration evidence",
                "No maven-compiler-plugin entry was detected in build plugins",
                "Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.",
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
                FindingSeverity.INFO,
                "Migration automation",
                "OpenRewrite has a Java " + context.request().targetJavaVersion() + " migration recipe",
                "Target Java version is " + context.request().targetJavaVersion(),
                "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                recipe));
    }

    private static boolean targetsJava17OrLater(RuleContext context) {
        return context.request().targetJavaVersion() >= 17;
    }

    private static boolean declaresJava8(ProjectMetadata metadata) {
        return "8".equals(metadata.declaredJavaVersion()) || "1.8".equals(metadata.declaredJavaVersion());
    }
}
