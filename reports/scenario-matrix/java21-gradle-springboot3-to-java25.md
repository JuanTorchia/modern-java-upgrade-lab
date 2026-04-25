# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-3-gradle-java-21`

## Executive Summary

- Readiness risk: LOW (15/100)
- Migration blockers: 0
- Recommended work items: 2
- Decision: Proceed with baseline validation before optional modernization.

## Project Summary

- Build tool: Gradle
- Declared Java version: 21
- Target Java version: 25
- Migration status: Upgrade required (Java 21 -> 25)
- Spring Boot version: 3.3.5

## Risk Assessment

- Risk level: LOW
- Risk score: 15/100
- Reason: Declared Java 21 targets Java 25

## Build Readiness

- Build wrapper present: No
- CI provider: Unknown
- CI evidence: `Unknown`
- Suggested test command: `gradle test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T02:18:17.979989Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-3-gradle-java-21`
- Git commit: `2ad9da582f0a89f8212893298bf176e9e1dde307`
- Git branch: `master`
- Target Java: 25

## Dependency & Plugin Baselines

| Category | Name | Version | Evidence |
| --- | --- | --- | --- |
| Build plugin | org.springframework.boot | 3.3.5 | `build.gradle.kts` |
| Build plugin | io.spring.dependency-management | 1.1.6 | `build.gradle.kts` |

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Priority: P0
- Phase: Baseline
- Command: `gradle test`

### [AUTOMATION] Run OpenRewrite Java 25 recipe in a branch

- Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
- Priority: P2
- Phase: Automation
- Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Suggested Commands

- `gradle test`
- `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 3.3.5 against Java 25 before runtime rollout.
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

## Language Modernization

### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\main\java\dev\modernjava\upgrade\example\GradleGreetingController.java:11 contains `Map<String, Object> hello() {`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.

## Concurrency

### [INFO] ThreadLocal usage should be reviewed for scoped values

- Area: Concurrency modernization
- Evidence: src\main\java\dev\modernjava\upgrade\example\RequestContext.java:5 contains `private static final ThreadLocal<String> TENANT = new ThreadLocal<>();`
- Recommendation: Review whether this context propagation can move toward scoped values on Java 25. Do not rewrite automatically; validate lifecycle, framework integration, and request boundaries first.

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