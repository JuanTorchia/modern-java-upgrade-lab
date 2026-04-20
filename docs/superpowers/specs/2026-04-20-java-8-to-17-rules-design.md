# Java 8 To 17 Rules Design

## Context

The first MVP can inspect a Maven project and print a Markdown report, but the CLI still builds findings directly. That is useful for a scaffold, but weak for community growth because contributors need a clear place to add migration knowledge.

This iteration turns the MVP into the first real analyzer increment: a small rule engine and the first Java 8 to Java 17 findings.

## Goal

Create an extensible rule catalog for Java LTS migration findings, then move the current hardcoded CLI logic into analyzer rules focused on Java 8 to Java 17.

## Non-Goals

- No JavaParser source scanning yet.
- No Gradle support in this iteration.
- No HTML report redesign.
- No automatic source rewrites.
- No broad catalog of modern Java features.

## Recommended Approach

Use a lightweight rule engine inside `analyzer-core`.

Alternatives considered:

1. Keep rules in the CLI. This is fastest, but it makes the project look like a script instead of a tool and gives contributors no stable extension point.
2. Add a plugin system now. This sounds open source friendly, but it is too early and would create framework work before we have enough rules.
3. Add a simple in-process rule catalog. This keeps the MVP small, testable, and ready for community contributions. This is the chosen approach.

## Architecture

`analyzer-core` will own:

- `MigrationRule`: one rule that can produce zero or more findings.
- `RuleContext`: target version plus project metadata.
- `RuleEngine`: runs a list of rules and returns findings.
- `DefaultAnalyzer`: implements `Analyzer` by running the rule engine.
- `Java8To17Rules`: first catalog for Java 8 to Java 17 migration.

`cli` will still own command-line parsing and Maven inspection, but it will stop constructing findings directly. It will create an `AnalysisRequest`, inspect Maven metadata, call `DefaultAnalyzer`, and render the result.

`rewrite-adapter` stays separate. For now, the Java 17 OpenRewrite recipe recommendation can live as a rule in `analyzer-core` because findings only store a recipe id string. A future iteration can decide whether OpenRewrite suggestions become their own analyzer module.

## Initial Rules

### Declared Java 8 Baseline

When a project declares Java `8` or `1.8` and the target is 17 or higher, emit an `INFO` finding explaining that the migration should first establish a reliable Java 17 build and test baseline.

### Spring Boot 2 On Java 17

When Spring Boot 2.x is detected and the target is 17 or higher, emit a `RISK` finding. The recommendation should be practical: validate framework compatibility, keep Spring Boot 2.7 as the last 2.x line if staying there temporarily, and plan Boot 3 separately because that introduces Jakarta changes.

### Missing Compiler Plugin Inventory

When Maven is detected but no `maven-compiler-plugin` appears in build plugins, emit an `INFO` finding recommending explicit compiler configuration. This is not a blocker, but it improves migration evidence.

### Java 17 OpenRewrite Recipe

When target is 17, emit an `INFO` finding with `org.openrewrite.java.migrate.UpgradeToJava17`.

## Testing

Tests should prove:

- the rule engine returns findings from multiple rules in deterministic order;
- Java 8 plus Spring Boot 2 targeting 17 produces the expected risk and automation findings;
- target versions outside the rule scope do not produce Java 8 to 17 findings;
- the CLI output still contains Java metadata and now includes Java 17 migration findings.

## Reporting

Keep the current Markdown shape for this iteration. Do not redesign sections yet. The priority is rule architecture and useful findings.

## Bitacora

Add `docs/bitacora/006-primeras-reglas-java-8-a-17.md` describing why the project moved from a hardcoded CLI demo to a rule-based analyzer.

## Acceptance Criteria

- Rules are no longer hardcoded in `AnalyzeCommand`.
- `analyzer-core` contains a small, tested rule engine.
- `mjul analyze --path <fixture> --target 17` reports Java 8 to 17 findings.
- Full Maven test suite passes on JDK 25.
- The iteration is documented in the bitacora.
