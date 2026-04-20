package dev.modernjava.upgrade.core;

import java.util.List;

public final class Java8To17Rules {

    private static final String MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins:maven-compiler-plugin";

    private Java8To17Rules() {
    }

    public static List<MigrationRule> defaults() {
        return List.of(
                Java8To17Rules::java8Baseline,
                Java8To17Rules::springBoot2Compatibility,
                Java8To17Rules::explicitMavenCompilerPlugin,
                Java8To17Rules::openRewriteJava17Recipe);
    }

    private static List<Finding> java8Baseline(RuleContext context) {
        if (!targetsJava17OrLater(context) || !declaresJava8(context.metadata())) {
            return List.of();
        }

        return List.of(new Finding(
                "java-8-baseline-target-17",
                FindingSeverity.INFO,
                "Java baseline",
                "Java 8 baseline should be migrated deliberately before adopting Java 17",
                "Declared Java version is " + context.metadata().declaredJavaVersion(),
                "Establish a Java 17 build and test baseline before introducing optional language modernization.",
                "org.openrewrite.java.migrate.UpgradeToJava17"));
    }

    private static List<Finding> springBoot2Compatibility(RuleContext context) {
        var springBootVersion = context.metadata().springBootVersion();
        if (!targetsJava17OrLater(context) || springBootVersion == null || !springBootVersion.startsWith("2.")) {
            return List.of();
        }

        return List.of(new Finding(
                "spring-boot-2-java-17-risk",
                FindingSeverity.RISK,
                "Spring Boot compatibility",
                "Spring Boot 2.x needs compatibility validation before a Java 17 migration",
                "Detected Spring Boot " + springBootVersion,
                "Validate the project on Spring Boot 2.7.x before the Java 17 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.",
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
                "Maven compiler configuration should be explicit for Java 17 migration evidence",
                "No maven-compiler-plugin entry was detected in build plugins",
                "Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.",
                null));
    }

    private static List<Finding> openRewriteJava17Recipe(RuleContext context) {
        if (context.request().targetJavaVersion() != 17) {
            return List.of();
        }

        return List.of(new Finding(
                "openrewrite-java-17",
                FindingSeverity.INFO,
                "Migration automation",
                "OpenRewrite has a Java 17 migration recipe",
                "Target Java version is 17",
                "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                "org.openrewrite.java.migrate.UpgradeToJava17"));
    }

    private static boolean targetsJava17OrLater(RuleContext context) {
        return context.request().targetJavaVersion() >= 17;
    }

    private static boolean declaresJava8(ProjectMetadata metadata) {
        return "8".equals(metadata.declaredJavaVersion()) || "1.8".equals(metadata.declaredJavaVersion());
    }
}
