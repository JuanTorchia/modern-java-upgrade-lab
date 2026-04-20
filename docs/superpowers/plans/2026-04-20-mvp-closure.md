# MVP Closure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the MVP CLI easier to demo and safer to run by adding report output files, friendly errors, CI, and release-style docs.

**Architecture:** Keep analysis internals unchanged. Improve `AnalyzeCommand` as the UX boundary and add CI/docs around the existing Maven reactor.

**Tech Stack:** Java 25, Maven, JUnit 5, AssertJ, picocli, GitHub Actions.

---

### Task 1: CLI Output And Error Tests

**Files:**
- Modify: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`

- [ ] Add a temp-dir based test for `--output`.
- [ ] Add a test for unsupported project path returning exit code `1`.
- [ ] Run `mvn -pl cli -am -Dtest=AnalyzeCommandTest "-Dsurefire.failIfNoSpecifiedTests=false" test`.
- [ ] Confirm RED because `--output` and friendly errors do not exist yet.

### Task 2: CLI Output And Error Implementation

**Files:**
- Modify: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`

- [ ] Add `--output`.
- [ ] Write report to output path when present, creating parent directories.
- [ ] Catch `IllegalArgumentException` and `UncheckedIOException`, print concise error to stderr, return `1`.
- [ ] Run focused CLI tests and confirm GREEN.

### Task 3: CI And Docs

**Files:**
- Create: `.github/workflows/ci.yml`
- Modify: `README.md`
- Modify: `docs/bitacora/000-index.md`
- Create: `docs/bitacora/010-cierre-del-mvp-cli.md`

- [ ] Add GitHub Actions workflow that sets up Java 25 and runs `mvn test`.
- [ ] Update README with package, stdout, and `--output` examples.
- [ ] Add bitacora entry explaining why this closes the first usable MVP.

### Task 4: Verification And Integration

**Files:** none

- [ ] Run full verification:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn test; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; mvn -pl cli -am package
```

- [ ] Smoke test `--output` with the shaded jar.
- [ ] Review diff.
- [ ] Merge fast-forward to `master`.
- [ ] Remove worktree and delete feature branch.
