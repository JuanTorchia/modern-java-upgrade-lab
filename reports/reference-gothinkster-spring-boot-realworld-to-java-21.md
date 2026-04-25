# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\gothinkster-spring-boot-realworld`

## Project Summary

- Build tool: Gradle
- Declared Java version: 11
- Target Java version: 21
- Migration status: Upgrade required (Java 11 -> 21)
- Spring Boot version: 2.6.3

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-24T02:05:13.872665400Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\gothinkster-spring-boot-realworld`
- Git commit: `ee17e31aafe733d98c4853c8b9a74d7f2f6c924a`
- Git branch: `master`
- Target Java: 21

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 2.6.3 support before moving to Java 21.
- Move to Spring Boot 2.7.x first when staying on the Spring Boot 2 line.
- Treat Spring Boot 3.x as a separate migration because it introduces Jakarta namespace changes.

### Phase 3: Automated Changes

- Run suggested OpenRewrite recipes in a dedicated branch.
- Review generated diffs and datatables before merging.

### Phase 4: Manual Review

- Review source modernization candidates after the migration baseline is stable.
- Keep optional refactors out of the baseline migration branch.

### Phase 5: Rollout

- Validate CI, container images, runtime flags, observability, and rollback paths.
- Roll out the runtime upgrade separately from broad application refactors.

## Framework Compatibility

### [RISK] Spring Boot 2.x needs compatibility validation before a Java 21 migration

- Area: Spring Boot compatibility
- Evidence: Detected Spring Boot 2.6.3
- Recommendation: Validate the project on Spring Boot 2.7.x before the Java 21 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.

## Language Modernization

### [INFO] Map-based responses can be reviewed as explicit DTOs or records

- Area: Language modernization
- Evidence: Detected 17 occurrences; examples: src\main\java\io\spring\api\ArticleApi.java:81; src\main\java\io\spring\api\ArticleFavoriteApi.java:53; src\main\java\io\spring\api\ArticlesApi.java:33
- Recommendation: Review the repeated response-shape pattern. Start by modeling the most stable API responses as DTOs and consider records after the migration baseline is stable.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21 -Drewrite.exportDatatables=true`