# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-2-java-8`

## Executive Summary

- Readiness risk: HIGH (85/100)
- Migration blockers: 4
- Recommended work items: 2
- Decision: Do not treat this as a language-only upgrade; resolve blockers before rollout.

## Project Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 25
- Migration status: Upgrade required (Java 8 -> 25)
- Spring Boot version: 2.7.18

## Risk Assessment

- Risk level: HIGH
- Risk score: 85/100
- Reason: Declared Java 8 targets Java 25
- Reason: Java 8 to Java 25 crosses multiple LTS baselines
- Reason: Report contains 4 risk-severity finding(s)

## Build Readiness

- Build wrapper present: No
- CI provider: Unknown
- CI evidence: `Unknown`
- Suggested test command: `mvn test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T02:17:58.764718900Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-2-java-8`
- Git commit: `2ad9da582f0a89f8212893298bf176e9e1dde307`
- Git branch: `master`
- Target Java: 25

## Dependency & Plugin Baselines

| Category | Name | Version | Evidence |
| --- | --- | --- | --- |
| Build plugin | org.apache.maven.plugins:maven-surefire-plugin | 2.19.1 | `pom.xml` |

## Migration Blockers

- [FRAMEWORK] Spring Boot 2.x needs compatibility validation before a Java 25 migration - Detected Spring Boot 2.7.18
- [BUILD] maven-surefire-plugin should be upgraded before Java 25 test baselining - Detected org.apache.maven.plugins:maven-surefire-plugin 2.19.1 in pom.xml
- [BUILD] Reflective access should be reviewed before Java 11+ migration - src\main\java\dev\modernjava\upgrade\example\LegacyReflectiveAccess.java:8 contains `field.setAccessible(true);`
- [BUILD] Removed Java EE/JAXB API usage blocks a clean Java 11 migration - src\main\java\dev\modernjava\upgrade\example\LegacyXmlBindingAdapter.java:3 contains `import javax.xml.bind.JAXBContext;`

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Priority: P0
- Phase: Baseline
- Command: `mvn test`

### [AUTOMATION] Run OpenRewrite Java 25 recipe in a branch

- Rationale: OpenRewrite automation should be reviewable and separate from manual runtime changes.
- Priority: P2
- Phase: Automation
- Command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Suggested Commands

- `mvn test`
- `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 2.7.18 support before moving to Java 25.
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

### [RISK] maven-surefire-plugin should be upgraded before Java 25 test baselining

- Area: Test runtime
- Evidence: Detected org.apache.maven.plugins:maven-surefire-plugin 2.19.1 in pom.xml
- Recommendation: Upgrade Surefire/Failsafe to a Java 25 compatible baseline before trusting migration test results.
### [INFO] Maven compiler configuration should be explicit for Java 25 migration evidence

- Area: Build configuration
- Evidence: No maven-compiler-plugin entry was detected in build plugins
- Recommendation: Add explicit compiler plugin configuration or verify the effective POM so the report can explain the Java release used by CI.
### [RISK] Reflective access should be reviewed before Java 11+ migration

- Area: Illegal reflective access
- Evidence: src\main\java\dev\modernjava\upgrade\example\LegacyReflectiveAccess.java:8 contains `field.setAccessible(true);`
- Recommendation: Run tests on Java 11 with warnings enabled and remove or explicitly contain illegal reflective access.
### [RISK] Removed Java EE/JAXB API usage blocks a clean Java 11 migration

- Area: Removed Java EE modules
- Evidence: src\main\java\dev\modernjava\upgrade\example\LegacyXmlBindingAdapter.java:3 contains `import javax.xml.bind.JAXBContext;`
- Recommendation: Add explicit dependencies or migrate the affected API before moving the runtime to Java 11.

## Framework Compatibility

### [RISK] Spring Boot 2.x needs compatibility validation before a Java 25 migration

- Area: Spring Boot compatibility
- Evidence: Detected Spring Boot 2.7.18
- Recommendation: Validate the project on Spring Boot 2.7.x before the Java 25 rollout. Treat Spring Boot 3 as a separate migration because it also introduces Jakarta namespace changes.

## Language Modernization

### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\main\java\dev\modernjava\upgrade\example\LegacyGreetingController.java:13 contains `public Map<String, Object> greeting() {`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 25 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 25
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25 -Drewrite.exportDatatables=true`

## Baseline & Planning

### [INFO] Java 8 baseline should be migrated deliberately before adopting Java 25

- Area: Java baseline
- Evidence: Declared Java version is 8
- Recommendation: Establish a Java 25 build and test baseline before introducing optional language modernization.