# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\realworld-springboot-java`

## Project Summary

- Build tool: Gradle
- Declared Java version: 11
- Target Java version: 21
- Migration status: Upgrade required (Java 11 -> 21)
- Spring Boot version: 2.5.2

## Risk Assessment

- Risk level: HIGH
- Risk score: 90/100
- Reason: Declared Java 11 targets Java 21
- Reason: Spring Boot 2.5.2 is below the safer 2.7.x Java 21 staging baseline
- Reason: Report contains 1 risk-severity finding(s)
- Reason: Gradle wrapper 6.8.3 should be validated before Java 21 builds
- Reason: Runtime image still references Java 11

## Build Readiness

- Build wrapper present: Yes
- CI provider: GitHub Actions
- CI evidence: `.github/workflows/build.yml`
- Suggested test command: `./gradlew test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T00:53:42.699339800Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\reference-cases\realworld-springboot-java`
- Git commit: `56be3ced4f3134424ead5fcaf387b3aa640b9532`
- Git branch: `master`
- Target Java: 21

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

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Command: `./gradlew test`

### [FRAMEWORK] Stage Spring Boot 2.7.x before the Java 21 rollout

- Rationale: Spring Boot 2.5.2 is below the safer Spring Boot 2.7.x staging baseline for Java 21 planning.

### [BUILD] Validate or upgrade the Gradle wrapper before Java 21

- Rationale: Detected Gradle wrapper 6.8.3. Older Gradle versions can block newer Java toolchains.
- Command: `./gradlew wrapper --gradle-version <validated-version>`

### [RUNTIME] Replace Java 11 runtime image before Java 21 rollout

- Rationale: Detected runtime image openjdk:11.0.10-jre-buster, which does not match the requested Java 21 runtime.

### [AUTOMATION] Run OpenRewrite Java 21 recipe in a branch

- Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
- Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 2.5.2 support before moving to Java 21.
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
- Evidence: Detected Spring Boot 2.5.2
- Recommendation: Validate the project on Spring Boot 2.7.x before the Java 21 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21 -Drewrite.exportDatatables=true`