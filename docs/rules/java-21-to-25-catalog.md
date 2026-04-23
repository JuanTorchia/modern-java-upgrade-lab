# Java 21 to 25 Migration Rule Catalog

This catalog defines candidate rules for Java 21 -> Java 25 migration reports.

The goal is not to promote Java 25 features by default. The goal is to define evidence-backed checks that help senior engineers decide what to review during a migration from the previous widely adopted LTS baseline.

## Source Baseline

Primary sources:

- [OpenJDK JDK 25 project page](https://openjdk.org/projects/jdk/25/)
- [OpenJDK JEPs in JDK 25 integrated since JDK 21](https://openjdk.org/projects/jdk/25/jeps-since-jdk-21)

OpenJDK lists JDK 25 General Availability on 2025-09-16 and describes it as an LTS release from most vendors. The JDK 25 project page and the JEPs-since-JDK-21 page are the canonical scope for this first catalog.

## Classification Model

Each rule candidate uses these fields:

- `id`: stable rule identifier.
- `transition`: migration path.
- `category`: current report category or proposed future category.
- `status`: `candidate`, `defer`, or `non-goal`.
- `feature maturity`: `final`, `preview`, `incubator`, `experimental`, or `removal`.
- `evidence`: what the analyzer must observe.
- `implementation path`: current metadata, textual source scanning, parser-backed source scanning, build-model extension, runtime/JFR integration, or documentation-only.
- `severity`: expected initial severity.
- `recommendation`: report text direction.

## Rule Selection Principles

- Prefer migration readiness over feature advocacy.
- Prefer observable evidence over generic suggestions.
- Do not recommend preview or incubator APIs as default modernization targets.
- Treat runtime and GC recommendations as measurement prompts, not static prescriptions.
- Keep Java 25 guidance separate from Java 21 adoption work. Many teams will still get most migration value by stabilizing on Java 21 first.

## Candidate Rules

| id | transition | category | status | feature maturity | evidence | implementation path | severity | recommendation |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `java-21-to-25-baseline-review` | 21 -> 25 | `BASELINE` | candidate | final release baseline | Declared Java version is `21`; target is `25`. | current metadata | `INFO` | Establish a Java 25 build and test baseline before enabling optional language, runtime, or GC changes. |
| `jdk-25-preview-feature-boundary` | 21 -> 25 | `BUILD` | candidate | preview/incubator | Build enables preview features or compiles with `--enable-preview`. | build-model extension | `RISK` | Treat preview/incubator feature usage as explicit technical debt. Verify source/bytecode compatibility on every JDK update. |
| `threadlocal-to-scoped-values-review` | 21 -> 25 | `CONCURRENCY` | candidate | final | Source uses `ThreadLocal` in request, security, tenant, trace, or transaction context paths. | textual source scanning first; parser-backed later | `INFO` | Review whether context propagation should move toward scoped values. Do not rewrite automatically; validate lifecycle and framework integration. |
| `structured-concurrency-preview-boundary` | 21 -> 25 | `CONCURRENCY` | candidate | preview | Source imports or references structured concurrency preview APIs, or build enables preview. | parser-backed source scanning plus build-model extension | `RISK` | Keep structured concurrency behind explicit preview boundaries. Do not present it as stable migration work. |
| `virtual-thread-synchronization-reprofile` | 21 -> 25 | `PERFORMANCE` | candidate | final runtime improvement since 21 | Project uses virtual threads and synchronized/blocking code paths. | parser-backed source scanning plus optional JFR integration | `INFO` | Re-profile virtual-thread workloads on Java 25. Runtime behavior changed since Java 21, but performance conclusions require workload evidence. |
| `unsafe-memory-access-warning` | 21 -> 25 | `BUILD` | candidate | deprecation/warning path | Source or dependencies reference `sun.misc.Unsafe` memory-access methods. | textual source scanning for direct use; dependency insight later | `RISK` | Remove direct unsafe memory-access usage where possible and audit dependencies that emit JDK warnings. |
| `jfr-java-25-observability-review` | 21 -> 25 | `OBSERVABILITY` | candidate | final/experimental JFR additions | Target is Java 25; project has performance-sensitive services or existing JFR usage. | documentation-only first; future JFR lab integration | `INFO` | Add a Java 25 JFR profiling plan before making performance claims. Consider CPU-time profiling, cooperative sampling, and method tracing only with workload-specific goals. |
| `compact-object-headers-measurement` | 21 -> 25 | `PERFORMANCE` | candidate | final JDK feature | Runtime flags enable compact object headers, or project is memory-density sensitive. | runtime config detection later | `INFO` | Evaluate compact object headers with heap, allocation, GC, and latency measurements. Do not recommend enabling purely from static analysis. |
| `shenandoah-generational-review` | 21 -> 25 | `PERFORMANCE` | candidate | final GC addition | Runtime flags use Shenandoah, or deployment docs mention Shenandoah. | runtime config detection later | `INFO` | If using Shenandoah, evaluate generational mode on representative workloads. Keep GC guidance measurement-based. |
| `aot-startup-review` | 21 -> 25 | `PERFORMANCE` | defer | final AOT additions | Application has startup-sensitive deployment shape, such as CLI, serverless, short-lived jobs, or documented startup SLOs. | documentation-only first; runtime/build integration later | `INFO` | Treat AOT command-line ergonomics and method profiling as startup experiments, not default migration steps. |
| `module-import-declarations-review` | 21 -> 25 | `LANGUAGE` | defer | final language feature | Source has large sets of imports from the same module or educational/demo source files. | parser-backed source scanning | `INFO` | Consider module import declarations only where they improve readability. Do not apply to production code as a broad migration rule. |
| `compact-source-files-boundary` | 21 -> 25 | `LANGUAGE` | defer | final language feature | Source files use compact source file style or instance main methods. | parser-backed source scanning | `INFO` | Keep compact source files focused on scripts, examples, and educational code unless the project explicitly adopts that style. |
| `flexible-constructor-bodies-review` | 21 -> 25 | `LANGUAGE` | defer | final language feature | Constructors contain validation or normalization that would benefit from statements before `super(...)`. | parser-backed source scanning | `INFO` | Review manually; this is a readability refactoring, not a migration blocker. |
| `crypto-kdf-api-review` | 21 -> 25 | `SECURITY` | defer | final API | Source or dependencies implement custom key derivation logic or use external KDF libraries. | parser-backed source scanning plus dependency insight | `INFO` | Review whether the JDK Key Derivation Function API can replace custom or third-party code. Require security review before changes. |
| `x86-32-port-removal` | 21 -> 25 | `BUILD` | candidate | removal | Build, CI, packaging, or deployment metadata targets 32-bit x86 JDKs. | build/deployment metadata extension | `RISK` | Remove 32-bit x86 JDK assumptions. Java 25 no longer includes that OpenJDK port. |

## Initial Implementation Order

1. `java-21-to-25-baseline-review`
   - Low risk.
   - Uses current metadata.
   - Provides immediate Java 25 report value without feature hype.

2. `threadlocal-to-scoped-values-review`
   - Useful source-level modernization candidate.
   - Can start with textual scanning for direct `ThreadLocal` usage.
   - Should stay advisory.

3. `jdk-25-preview-feature-boundary`
   - Important for senior teams because preview usage affects CI, release, and support posture.
   - Requires build-model extension to capture compiler arguments.

4. `unsafe-memory-access-warning`
   - Relevant to real migrations because warnings surface during builds/tests.
   - Direct source usage can be detected now; dependency-origin warnings require future work.

5. `jfr-java-25-observability-review`
   - Best implemented first as documentation/report guidance.
   - Later can become a JFR lab or runtime analysis feature.

## Explicit Non-Goals

- Do not recommend preview or incubator APIs as default modernization work.
- Do not claim Java 25 performance improvements without workload evidence.
- Do not encode a full vendor support matrix in the analyzer.
- Do not automatically rewrite source code for language features in this phase.
- Do not treat compact source files, module imports, or flexible constructor bodies as migration blockers.
- Do not infer framework compatibility from JDK version alone.

## Follow-Up Issues

This catalog should produce smaller implementation issues:

- Add `java-21-to-25-baseline-review`.
- Add textual `ThreadLocal` source detection for scoped-values review.
- Extend build inspectors to capture compiler preview flags.
- Add direct `sun.misc.Unsafe` source usage detection.
- Define a JFR lab/report section for Java 25 observability checks.
