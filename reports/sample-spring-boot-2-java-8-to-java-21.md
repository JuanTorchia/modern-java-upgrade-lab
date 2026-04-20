# Modern Java Upgrade Report

Project path: `examples/spring-boot-2-java-8`

## Project Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 21
- Spring Boot version: 2.7.18

## Build & Tooling

### [INFO] Maven compiler configuration should be explicit for Java 21 migration evidence

- Area: Build configuration
- Evidence: No maven-compiler-plugin entry was detected in build plugins
- Recommendation: Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.

## Framework Compatibility

### [RISK] Spring Boot 2.x needs compatibility validation before a Java 21 migration

- Area: Spring Boot compatibility
- Evidence: Detected Spring Boot 2.7.18
- Recommendation: Validate the project on Spring Boot 2.7.x before the Java 21 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21 -Drewrite.exportDatatables=true`

## Baseline & Planning

### [INFO] Java 8 baseline should be migrated deliberately before adopting Java 21

- Area: Java baseline
- Evidence: Declared Java version is 8
- Recommendation: Establish a Java 21 build and test baseline before introducing optional language modernization.
