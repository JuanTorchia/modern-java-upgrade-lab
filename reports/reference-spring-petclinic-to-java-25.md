# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\spring-petclinic`

## Project Summary

- Build tool: Maven
- Declared Java version: 17
- Target Java version: 25
- Migration status: Upgrade required (Java 17 -> 25)
- Spring Boot version: 4.0.3

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-24T02:04:58.912329900Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\spring-petclinic`
- Git commit: `c7ee170434ec3e369fdc9201290ba2ea4c92b557`
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

## Build & Tooling

### [INFO] Maven compiler configuration should be explicit for Java 25 migration evidence

- Area: Build configuration
- Evidence: No maven-compiler-plugin entry was detected in build plugins
- Recommendation: Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.

## Language Modernization

### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\main\java\org\springframework\samples\petclinic\owner\VisitController.java:64 contains `Map<String, Object> model) {`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.
### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\test\java\org\springframework\samples\petclinic\system\CrashControllerIntegrationTests.java:63 contains `ResponseEntity<Map<String, Object>> resp = rest.exchange(`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 25 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 25
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25 -Drewrite.exportDatatables=true`