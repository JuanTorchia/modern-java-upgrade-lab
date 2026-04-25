# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\jhipster-sample-app`

## Project Summary

- Build tool: Maven
- Declared Java version: 21
- Target Java version: 25
- Migration status: Upgrade required (Java 21 -> 25)
- Spring Boot version: 4.0.3

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-24T02:05:05.334751700Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\jhipster-sample-app`
- Git commit: `1b0730f2d7ad13bb1c71396692b86bb9205e8d7c`
- Git branch: `main`
- Target Java: 25

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 4.0.3 against Java 25 before runtime rollout.
- Keep framework upgrades separate from optional language refactors.

### Phase 3: Automated Changes

- Run suggested OpenRewrite recipes in a dedicated branch.
- Review generated diffs and datatables before merging.

### Phase 4: Manual Review

- Review source modernization candidates after the migration baseline is stable.
- Keep optional refactors out of the baseline migration branch.

### Phase 5: Rollout

- Validate CI, container images, runtime flags, observability, and rollback paths.
- Roll out the runtime upgrade separately from broad application refactors.

## Language Modernization

### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\main\java\io\github\jhipster\sample\web\rest\errors\ExceptionTranslator.java:124 contains `Map<String, Object> problemProperties = problem.getProperties();`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 25 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 25
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25 -Drewrite.exportDatatables=true`

## Baseline & Planning

### [INFO] Java 21 to 25 migration should start with a build and test baseline

- Area: Java baseline
- Evidence: Declared Java version is 21; target Java version is 25
- Recommendation: Establish a Java 25 build and test baseline before enabling optional language, runtime, GC, JFR, or AOT changes.