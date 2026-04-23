# 013 - Java 17 to 21 Spring Boot Baseline Rule

## Date

2026-04-22

## Goal

Implement issue #1: add a focused Java 17 -> Java 21 migration rule that reminds teams to validate their Spring Boot baseline before runtime rollout.

## Decisions

- Add the rule in `analyzer-core`, where migration knowledge currently lives.
- Trigger the rule only when the project declares Java 17, targets Java 21, and has a detected Spring Boot version.
- Do not duplicate the existing Spring Boot 2.x risk rule. Spring Boot 2.x keeps the stronger existing compatibility warning.
- Use `INFO` severity because this rule is an advisory review gate, not proof of incompatibility.

## Rejected Options

- Do not encode a full Spring Boot support matrix in this first issue. That would require broader framework-version policy and maintenance.
- Do not make the rule target every Java 21 migration. The issue is specifically about Java 17 -> 21 projects.
- Do not imply that the analyzer fully verifies runtime compatibility.

## Rationale

The rule improves the migration report by making framework baseline review explicit. For senior engineers, the useful output is not "Java 21 is available"; it is "before the runtime rollout, validate the framework line, dependency baselines, and CI runtime assumptions."

## Implementation Notes

I followed TDD:

1. Added a failing analyzer test expecting `spring-boot-java-17-to-21-baseline-review`.
2. Verified RED: the report only contained `openrewrite-java-21`.
3. Added `springBootJava17To21BaselineReview` to `DefaultMigrationRules`.
4. Verified GREEN in `analyzer-core`.

## Verification

Module verification:

```powershell
mvn -pl analyzer-core test
```

Result: 15 analyzer-core tests passed.

Full reactor verification:

```powershell
mvn test
```

Result: 32 tests passed across the reactor. Maven still emits the known JDK 25 `sun.misc.Unsafe` warning from Maven/Guice, but the build succeeds.

## Content Angle

This rule is a small but useful example for contributors: a migration rule should be scoped, evidence-backed, advisory when appropriate, and explicit about what it does not prove.

## Next Step

Run the full test suite, push the branch, open a pull request, and close issue #1 through the PR.
