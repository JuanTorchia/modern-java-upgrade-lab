# Gradle Support MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add conservative Gradle project inspection and wire it into the existing CLI analysis flow.

**Architecture:** Add `GradleProjectInspector` beside `MavenProjectInspector`, then introduce a small `ProjectInspector` selector used by the CLI. Keep Gradle parsing textual and bounded to common build file patterns.

**Tech Stack:** Java 25, Maven reactor, JUnit 5, AssertJ, picocli.

---

### Task 1: Gradle Fixtures And RED Tests

**Files:**
- Create: `build-inspectors/src/test/resources/fixtures/gradle-groovy-java17-springboot2/build.gradle`
- Create: `build-inspectors/src/test/resources/fixtures/gradle-kotlin-java21-springboot3/build.gradle.kts`
- Create: `build-inspectors/src/test/java/dev/modernjava/upgrade/build/GradleProjectInspectorTest.java`

- [ ] Create Gradle fixtures with Java version, Spring Boot plugin, dependencies and plugins.
- [ ] Write tests expecting `buildTool`, `declaredJavaVersion`, `springBootVersion`, dependencies and plugins.
- [ ] Run `mvn -pl build-inspectors -Dtest=GradleProjectInspectorTest test` and confirm RED because `GradleProjectInspector` does not exist.

### Task 2: Minimal Gradle Inspector

**Files:**
- Create: `build-inspectors/src/main/java/dev/modernjava/upgrade/build/GradleProjectInspector.java`
- Modify: `build-inspectors/src/test/java/dev/modernjava/upgrade/build/GradleProjectInspectorTest.java`

- [ ] Implement `inspect(Path)` with `build.gradle` / `build.gradle.kts` resolution.
- [ ] Detect Java version from `JavaVersion.VERSION_17`, string assignments and `JavaLanguageVersion.of(21)`.
- [ ] Detect Spring Boot version from `id 'org.springframework.boot' version 'x.y.z'` and `id("org.springframework.boot") version "x.y.z"`.
- [ ] Collect dependency coordinates and plugin ids.
- [ ] Run `mvn -pl build-inspectors -Dtest=GradleProjectInspectorTest test` and confirm GREEN.

### Task 3: Build Tool Selector

**Files:**
- Create: `build-inspectors/src/main/java/dev/modernjava/upgrade/build/ProjectInspector.java`
- Create: `build-inspectors/src/test/java/dev/modernjava/upgrade/build/ProjectInspectorTest.java`

- [ ] Write RED tests for Maven selection, Gradle selection and unknown build rejection.
- [ ] Implement `ProjectInspector.inspect(Path)` selecting Maven first, then Gradle.
- [ ] Run `mvn -pl build-inspectors test` and confirm GREEN.

### Task 4: CLI Integration And Example

**Files:**
- Modify: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`
- Modify: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`
- Create: `examples/spring-boot-3-gradle-java-21/build.gradle.kts`
- Create: `examples/spring-boot-3-gradle-java-21/settings.gradle.kts`
- Create: `examples/spring-boot-3-gradle-java-21/src/main/java/dev/modernjava/upgrade/example/GradleGreetingController.java`

- [ ] Write CLI test against the Gradle example expecting `Build tool: gradle`, `Declared Java version: 21` and `Spring Boot version: 3.3.5`.
- [ ] Wire CLI to `ProjectInspector`.
- [ ] Run `mvn -pl cli -am test` and confirm GREEN.

### Task 5: Docs, Bitacora, Sample Report

**Files:**
- Modify: `README.md`
- Modify: `docs/contributing-rules.md`
- Modify: `docs/bitacora/000-index.md`
- Create: `docs/bitacora/009-soporte-gradle-mvp.md`
- Create: `reports/sample-spring-boot-3-gradle-java-21-to-java-25.md`

- [ ] Document Gradle support as initial textual detection.
- [ ] Add bitacora entry in first person explaining decisions and tradeoffs.
- [ ] Generate a sample report using the jar or CLI command.

### Task 6: Verification And Integration

**Files:** none

- [ ] Run full verification:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn test; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; mvn -pl cli -am package
```

- [ ] Smoke test the Gradle example with the shaded jar.
- [ ] Review diff.
- [ ] Merge fast-forward to `master`.
- [ ] Remove worktree and delete feature branch.
