# Source Pattern Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Detect the first source-code modernization patterns and surface them as categorized migration findings.

**Architecture:** Add source pattern evidence to `ProjectMetadata`, scan `.java` files with a small deterministic scanner, and map detected patterns to analyzer findings. Keep this as evidence only; no source rewriting.

**Tech Stack:** Java 25, Maven, JUnit 5, AssertJ, picocli.

---

### Task 1: Add Source Pattern Model And Scanner

**Files:**
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/SourcePattern.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/SourcePatternType.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/SourcePatternScanner.java`
- Test: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/SourcePatternScannerTest.java`

- [ ] Write failing tests for detecting `Map<String, Object>`, `SimpleDateFormat`, and executor factories.
- [ ] Run `mvn -pl analyzer-core -Dtest=SourcePatternScannerTest test` and verify RED.
- [ ] Implement minimal scanner.
- [ ] Run scanner tests and verify GREEN.
- [ ] Commit `feat: scan Java source modernization patterns`.

### Task 2: Feed Source Patterns Into Analyzer Rules

**Files:**
- Modify: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/ProjectMetadata.java`
- Modify: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/DefaultMigrationRules.java`
- Test: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/DefaultAnalyzerTest.java`

- [ ] Write failing tests that source patterns become `LANGUAGE` and `CONCURRENCY` findings.
- [ ] Run analyzer tests and verify RED.
- [ ] Add `sourcePatterns` to metadata and rules.
- [ ] Run analyzer tests and verify GREEN.
- [ ] Commit `feat: report source modernization findings`.

### Task 3: Wire CLI And Example

**Files:**
- Modify: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`
- Modify: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`
- Modify: `reports/sample-spring-boot-2-java-8-to-java-21.md`

- [ ] Write failing CLI assertion for `Language Modernization`.
- [ ] Run CLI tests and verify RED.
- [ ] Scan source files in the CLI and enrich metadata.
- [ ] Run CLI tests and verify GREEN.
- [ ] Commit `feat: include source patterns in CLI analysis`.

### Task 4: Document Iteration

**Files:**
- Create: `docs/bitacora/008-deteccion-de-patrones-de-codigo.md`
- Modify: `docs/bitacora/000-index.md`
- Modify: `docs/contributing-rules.md`

- [ ] Document the first-person bitacora.
- [ ] Update contributor docs with source-pattern rule guidance.
- [ ] Run `mvn test`.
- [ ] Commit `docs: document source pattern detection`.
