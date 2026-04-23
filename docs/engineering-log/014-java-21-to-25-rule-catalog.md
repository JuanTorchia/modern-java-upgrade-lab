# 014 - Java 21 to 25 Rule Catalog

## Date

2026-04-22

## Goal

Implement issue #2 by defining the first Java 21 -> Java 25 migration rule catalog.

## Decisions

- Create `docs/rules/java-21-to-25-catalog.md` as a technical catalog, not analyzer code.
- Base the catalog on OpenJDK primary sources for JDK 25 and the JEPs integrated since JDK 21.
- Separate final, preview, incubator, experimental, and removal-related features.
- Mark each candidate with evidence requirements and implementation path.
- Prioritize rules that improve migration readiness without turning the project into Java 25 feature marketing.

## Rejected Options

- Do not implement Java 25 rules before defining the catalog. That would create ad hoc behavior and make contributor work harder to review.
- Do not recommend preview or incubator APIs as default modernization targets.
- Do not make static performance claims for GC, JFR, object headers, or AOT features.

## Rationale

Java 25 is useful as an LTS comparison point, but the project should not age around one release. The rule catalog keeps Java 21 -> 25 work grounded in evidence, maturity, and implementation feasibility.

The catalog also creates smaller contribution paths. Contributors can pick a specific rule with an explicit category, evidence model, severity, and non-goal.

## Concrete Result

The catalog defines candidate rules for:

- baseline migration review;
- preview feature boundaries;
- scoped values review from `ThreadLocal` evidence;
- structured concurrency preview boundaries;
- virtual-thread workload re-profiling;
- direct unsafe memory-access usage;
- JFR observability planning;
- compact object headers measurement;
- Generational Shenandoah review;
- AOT startup experiments;
- language features that should remain deferred or advisory;
- 32-bit x86 port removal.

## Verification

This is a documentation and planning change. I still verified it with the full test suite:

```powershell
mvn test
```

Result: 32 tests passed. Maven still emits the known JDK 25 `sun.misc.Unsafe` warning from Maven/Guice, but the build succeeds.

## Content Angle

This is the technical foundation for a blog post section: "How I decide whether a Java feature belongs in a migration report."

## Next Step

Open a PR that closes issue #2. After merge, convert the top catalog entries into small implementation issues.
