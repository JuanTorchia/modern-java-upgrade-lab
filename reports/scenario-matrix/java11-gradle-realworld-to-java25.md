# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\realworld-springboot-java`

## Executive Summary

- Readiness risk: HIGH (100/100)
- Migration blockers: 3
- Recommended work items: 5
- Decision: Do not treat this as a language-only upgrade; resolve blockers before rollout.

## Project Summary

- Build tool: Gradle
- Declared Java version: 11
- Target Java version: 25
- Migration status: Upgrade required (Java 11 -> 25)
- Spring Boot version: 2.5.2

## Risk Assessment

- Risk level: HIGH
- Risk score: 100/100
- Reason: Declared Java 11 targets Java 25
- Reason: Spring Boot 2.5.2 is below the safer 2.7.x Java 21 staging baseline
- Reason: Report contains 3 risk-severity finding(s)
- Reason: Gradle wrapper 6.8.3 should be validated before Java 25 builds
- Reason: Runtime image still references Java 11

## Build Readiness

- Build wrapper present: Yes
- CI provider: GitHub Actions
- CI evidence: `.github/workflows/build.yml`
- Suggested test command: `./gradlew test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T02:18:08.986419Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\realworld-springboot-java`
- Git commit: `56be3ced4f3134424ead5fcaf387b3aa640b9532`
- Git branch: `master`
- Target Java: 25

## Dependency & Plugin Baselines

| Category | Name | Version | Evidence |
| --- | --- | --- | --- |
| Build tool | Gradle wrapper | 6.8.3 | `gradle/wrapper/gradle-wrapper.properties` |
| Build plugin | org.springframework.boot | 2.5.2 | `build.gradle` |
| Build plugin | io.spring.dependency-management | 1.0.11.RELEASE | `build.gradle` |
| Build plugin | io.freefair.lombok | 5.3.3.3 | `build.gradle` |
| Build plugin | org.ec4j.editorconfig | 0.0.3 | `build.gradle` |
| Build plugin | org.sonarqube | 3.1.1 | `build.gradle` |
| Build plugin | com.google.cloud.tools.jib | 3.1.4 | `build.gradle` |
| Test dependency | org.mockito:mockito-inline | 3.12.1 | `build.gradle` |
| Runtime image | Jib base image | openjdk:11.0.10-jre-buster | `build.gradle` |

## Migration Blockers

- [FRAMEWORK] Spring Boot 2.x needs compatibility validation before a Java 25 migration - Detected Spring Boot 2.5.2
- [BUILD] mockito-inline baseline should be reviewed before Java 25 - Detected org.mockito:mockito-inline 3.12.1 in build.gradle
- [BUILD] Runtime image should match the requested Java 25 rollout - Detected runtime image openjdk:11.0.10-jre-buster in build.gradle

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Priority: P0
- Phase: Baseline
- Command: `./gradlew test`

### [FRAMEWORK] Stage Spring Boot 2.7.x before the Java 25 rollout

- Rationale: Spring Boot 2.5.2 is below the safer Spring Boot 2.7.x staging baseline for Java 25 planning.
- Priority: P1
- Phase: Framework

### [BUILD] Validate or upgrade the Gradle wrapper before Java 25

- Rationale: Detected Gradle wrapper 6.8.3. Older Gradle versions can block newer Java toolchains.
- Priority: P1
- Phase: Build
- Command: `./gradlew wrapper --gradle-version <validated-version>`

### [RUNTIME] Replace Java 11 runtime image before Java 25 rollout

- Rationale: Detected runtime image openjdk:11.0.10-jre-buster, which does not match the requested Java 25 runtime.
- Priority: P1
- Phase: Runtime

### [AUTOMATION] Run OpenRewrite Java 25 recipe in a branch

- Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
- Priority: P2
- Phase: Automation
- Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Suggested Commands

- `./gradlew test`
- `./gradlew wrapper --gradle-version <validated-version>`
- `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 2.5.2 support before moving to Java 25.
- Move to Spring Boot 2.7.x first when staying on the Spring Boot 2 line.
- Treat Spring Boot 3.x as a separate migration because it introduces Jakarta namespace changes.

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

### [RISK] mockito-inline baseline should be reviewed before Java 25

- Area: Dependency compatibility
- Evidence: Detected org.mockito:mockito-inline 3.12.1 in build.gradle
- Recommendation: Validate this dependency against the target JDK and upgrade it in a dedicated build-readiness branch.
### [RISK] Runtime image should match the requested Java 25 rollout

- Area: Runtime image
- Evidence: Detected runtime image openjdk:11.0.10-jre-buster in build.gradle
- Recommendation: Update container/runtime images separately from source changes and verify rollback to the previous runtime.

## Framework Compatibility

### [RISK] Spring Boot 2.x needs compatibility validation before a Java 25 migration

- Area: Spring Boot compatibility
- Evidence: Detected Spring Boot 2.5.2
- Recommendation: Validate the project on Spring Boot 2.7.x before the Java 25 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 25 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 25
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25 -Drewrite.exportDatatables=true`