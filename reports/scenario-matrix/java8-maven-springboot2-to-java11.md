# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-2-java-8`

## Executive Summary

- Readiness risk: MEDIUM (55/100)
- Migration blockers: 3
- Recommended work items: 1
- Decision: Do not treat this as a language-only upgrade; resolve blockers before rollout.

## Project Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 11
- Migration status: Upgrade required (Java 8 -> 11)
- Spring Boot version: 2.7.18

## Risk Assessment

- Risk level: MEDIUM
- Risk score: 55/100
- Reason: Declared Java 8 targets Java 11
- Reason: Java 8 to Java 11 requires removed-module and illegal-access validation
- Reason: Report contains 3 risk-severity finding(s)

## Build Readiness

- Build wrapper present: No
- CI provider: Unknown
- CI evidence: `Unknown`
- Suggested test command: `mvn test`

## Analysis Metadata

- Analyzer version: 0.1.0-SNAPSHOT
- Generated at: 2026-04-25T02:17:51.809704400Z
- Source path: `C:\Users\jstor\develop\modern-java-upgrade\examples\spring-boot-2-java-8`
- Git commit: `2ad9da582f0a89f8212893298bf176e9e1dde307`
- Git branch: `master`
- Target Java: 11

## Dependency & Plugin Baselines

| Category | Name | Version | Evidence |
| --- | --- | --- | --- |
| Build plugin | org.apache.maven.plugins:maven-surefire-plugin | 2.19.1 | `pom.xml` |

## Migration Blockers

- [BUILD] maven-surefire-plugin should be upgraded before Java 11 test baselining - Detected org.apache.maven.plugins:maven-surefire-plugin 2.19.1 in pom.xml
- [BUILD] Reflective access should be reviewed before Java 11+ migration - src\main\java\dev\modernjava\upgrade\example\LegacyReflectiveAccess.java:8 contains `field.setAccessible(true);`
- [BUILD] Removed Java EE/JAXB API usage blocks a clean Java 11 migration - src\main\java\dev\modernjava\upgrade\example\LegacyXmlBindingAdapter.java:3 contains `import javax.xml.bind.JAXBContext;`

## Recommended Work Items

### [BUILD] Run baseline tests in CI before migration changes

- Rationale: A Java migration needs a known failing/passing baseline before changing runtime, framework, or build configuration.
- Priority: P0
- Phase: Baseline
- Command: `mvn test`

## Suggested Commands

- `mvn test`

## Migration Plan

### Phase 1: Baseline

- Confirm the current Java version in local development and CI.
- Run the full test suite before changing the Java target.
- Make compiler, toolchain, and runtime configuration explicit.

### Phase 2: Framework Compatibility

- Validate Spring Boot 2.7.18 support before moving to Java 11.
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

### [RISK] maven-surefire-plugin should be upgraded before Java 11 test baselining

- Area: Test runtime
- Evidence: Detected org.apache.maven.plugins:maven-surefire-plugin 2.19.1 in pom.xml
- Recommendation: Upgrade Surefire/Failsafe to a Java 11 compatible baseline before trusting migration test results.
### [RISK] Reflective access should be reviewed before Java 11+ migration

- Area: Illegal reflective access
- Evidence: src\main\java\dev\modernjava\upgrade\example\LegacyReflectiveAccess.java:8 contains `field.setAccessible(true);`
- Recommendation: Run tests on Java 11 with warnings enabled and remove or explicitly contain illegal reflective access.
### [RISK] Removed Java EE/JAXB API usage blocks a clean Java 11 migration

- Area: Removed Java EE modules
- Evidence: src\main\java\dev\modernjava\upgrade\example\LegacyXmlBindingAdapter.java:3 contains `import javax.xml.bind.JAXBContext;`
- Recommendation: Add explicit dependencies or migrate the affected API before moving the runtime to Java 11.

## Baseline & Planning

### [INFO] Java 8 to 11 migration needs a compatibility baseline

- Area: Java baseline
- Evidence: Declared Java version is 8; target Java version is 11
- Recommendation: Run the full test suite on Java 8, then establish a separate Java 11 branch that validates removed Java EE modules, reflection warnings, build plugins, and runtime images.