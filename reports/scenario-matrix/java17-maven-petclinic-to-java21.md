# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\spring-petclinic`

## Executive Summary

- Readiness risk: LOW (15/100)
- Migration blockers: 0
- Recommended work items: 2
- Decision: Proceed with baseline validation before optional modernization.

## Project Summary

- Build tool: Maven
- Declared Java version: 17
- Target Java version: 21
- Migration status: Upgrade required (Java 17 -> 21)
- Spring Boot version: 4.0.3

## Risk Assessment

- Risk level: LOW
- Risk score: 15/100
- Reason: Declared Java 17 targets Java 21

## Build Readiness

- Build wrapper present: Yes
- CI provider: GitHub Actions
- CI evidence: `.github/workflows/deploy-and-test-cluster.yml`
- Suggested test command: `./mvnw test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T02:18:12.445149Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\spring-petclinic`
- Git commit: `c7ee170434ec3e369fdc9201290ba2ea4c92b557`
- Git branch: `main`
- Target Java: 21

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Priority: P0
- Phase: Baseline
- Command: `./mvnw test`

### [AUTOMATION] Run OpenRewrite Java 21 recipe in a branch

- Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
- Priority: P2
- Phase: Automation
- Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`

## Suggested Commands

- `./mvnw test`
- `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 4.0.3 against Java 21 before runtime rollout.
- Keep framework upgrades separate from optional language refactors.

### Phase 3: Automated Changes

- Run suggested OpenRewrite recipes in a dedicated branch.
- Review generated diffs and datatables before merging.

### Phase 4: Manual Review

- Review source modernization candidates after the migration baseline is stable.
- Keep optional refactors out of the baseline migration branch.

### Out-of-scope Modernization

- Do not combine broad DTO/record refactors with runtime rollout.
- Do not auto-rewrite concurrency primitives without lifecycle validation.

### Phase 5: Rollout

- Validate CI, container images, runtime flags, observability, and rollback paths.
- Roll out the runtime upgrade separately from broad application refactors.

## Build & Tooling

### [INFO] Maven compiler configuration should be explicit for Java 21 migration evidence

- Area: Build configuration
- Evidence: No maven-compiler-plugin entry was detected in build plugins
- Recommendation: Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.

## Framework Compatibility

### [INFO] Spring Boot baseline should be reviewed before a Java 21 rollout

- Area: Spring Boot compatibility
- Evidence: Declared Java version is 17; detected Spring Boot 4.0.3; target Java version is 21
- Recommendation: Validate the selected Spring Boot line against Java 21 before runtime rollout. Treat framework upgrades, dependency baselines, and CI runtime changes as explicit migration work rather than language modernization.

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

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21 -Drewrite.exportDatatables=true`