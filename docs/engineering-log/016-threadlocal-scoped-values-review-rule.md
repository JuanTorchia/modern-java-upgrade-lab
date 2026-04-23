# 016 - ThreadLocal Scoped Values Review Rule

## Date

2026-04-22

## Goal

Implement the second Java 21 -> Java 25 catalog item: detect direct `ThreadLocal` usage and report it as a scoped-values review candidate.

## Decisions

- Extend the existing textual source scanner with a `THREAD_LOCAL` pattern.
- Ignore imports as the scanner already does for other patterns.
- Generate a `CONCURRENCY` / `INFO` finding only for Java 21 -> Java 25 migrations.
- Keep the recommendation advisory and explicitly avoid automatic rewriting.

## Rejected Options

- Do not introduce JavaParser yet. Direct `ThreadLocal` usage is detectable with the current scanner and gives enough signal for an advisory finding.
- Do not include structured concurrency in this rule. Structured concurrency remains preview in Java 25 and needs a separate preview-boundary rule.
- Do not recommend replacing every `ThreadLocal`; framework integration and lifecycle boundaries matter.

## Rationale

Scoped values are final in Java 25 and are relevant to context propagation. However, `ThreadLocal` usage can be intentional, framework-managed, or safe in a given lifecycle.

The report should therefore surface the evidence and ask for review. It should not imply that scoped values are a universal replacement.

## Implementation Notes

I followed TDD:

1. Added a scanner test expecting `SourcePatternType.THREAD_LOCAL`.
2. Verified RED: the enum value did not exist.
3. Added the enum value and textual scanner detection.
4. Updated exhaustive switches in `DefaultMigrationRules`.
5. Added analyzer tests for Java 21 -> 25 positive behavior and negative behavior for Java 17 -> 25 and Java 21 -> 21.

## Verification

Scanner verification:

```powershell
mvn -pl analyzer-core -Dtest=SourcePatternScannerTest test
```

Result: 4 scanner tests passed.

Module verification:

```powershell
mvn -pl analyzer-core test
```

Result: 20 analyzer-core tests passed.

Full reactor verification:

```powershell
mvn test
```

Result: 37 tests passed. Maven still emits the known JDK 25 `sun.misc.Unsafe` warning from Maven/Guice, but the build succeeds.

## Content Angle

This rule is a useful example of conservative modernization guidance. The analyzer detects `ThreadLocal` evidence, but the recommendation stays explicit about human review and framework context.

## Next Step

Open a PR that closes the implementation issue. After merge, decide whether the next Java 25 rule should be preview-feature boundary detection or direct `sun.misc.Unsafe` source detection.
