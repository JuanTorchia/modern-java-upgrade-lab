# 015 - Java 21 to 25 Baseline Review Rule

## Date

2026-04-22

## Goal

Implement the first rule from the Java 21 -> Java 25 catalog: `java-21-to-25-baseline-review`.

## Decisions

- Add the rule to `DefaultMigrationRules`.
- Trigger only when the project declares Java 21 and the requested target is Java 25.
- Keep the rule `BASELINE` / `INFO`.
- Make the recommendation about establishing build and test confidence before optional Java 25 work.

## Rejected Options

- Do not recommend specific Java 25 features from this rule.
- Do not fire for Java 17 -> 25. That transition needs separate baseline logic because it includes both Java 21 and Java 25 adoption work.
- Do not make this a framework compatibility rule. The observable evidence is the Java baseline transition.

## Rationale

This is the lowest-risk implementation from the Java 21 -> 25 catalog. It gives Java 25 reports immediate value while keeping the project aligned with evidence-based migration guidance.

The recommendation deliberately mentions language, runtime, GC, JFR, and AOT as optional areas that should come after the baseline is green.

## Implementation Notes

I followed TDD:

1. Added a failing analyzer test expecting `java-21-to-25-baseline-review`.
2. Verified RED: no finding with that id was produced.
3. Added `java21To25BaselineReview` to `DefaultMigrationRules`.
4. Added negative coverage for Java 17 -> 25 and Java 21 -> 21.
5. Verified GREEN in `analyzer-core` and the full reactor.

## Verification

Module verification:

```powershell
mvn -pl analyzer-core test
```

Result: 17 analyzer-core tests passed.

Full reactor verification:

```powershell
mvn test
```

Result: 34 tests passed. Maven still emits the known JDK 25 `sun.misc.Unsafe` warning from Maven/Guice, but the build succeeds.

## Content Angle

This is a practical example of turning a rule catalog into analyzer behavior without expanding scope. The first Java 25 rule is not about new syntax; it is about creating a stable migration baseline.

## Next Step

Open a PR that closes the implementation issue, then move to the next catalog item: `threadlocal-to-scoped-values-review`.
