# CLI Usable Y Reportes Extensibles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable CLI jar and make Markdown reports extensible through explicit finding categories.

**Architecture:** Add a `FindingCategory` enum to `analyzer-core`, extend `Finding` with a category field, and update default rules plus the Markdown renderer to group findings into stable sections. Configure the `cli` module with Maven Shade so the command can run as a self-contained jar.

**Tech Stack:** Java 25, Maven, JUnit 5, AssertJ, picocli, Maven Shade Plugin.

---

### Task 1: Add Finding Categories

**Files:**
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/FindingCategory.java`
- Modify: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Finding.java`
- Modify: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/DefaultMigrationRules.java`
- Test: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/DefaultAnalyzerTest.java`
- Test: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/RuleEngineTest.java`

- [ ] **Step 1: Write failing tests**

Add assertions that each default finding has the expected category:

```java
assertThat(findings)
        .extracting(Finding::category)
        .contains(
                FindingCategory.BASELINE,
                FindingCategory.FRAMEWORK,
                FindingCategory.BUILD,
                FindingCategory.AUTOMATION);
```

- [ ] **Step 2: Run analyzer-core tests and verify RED**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl analyzer-core test
```

Expected: compilation fails because `FindingCategory` and `Finding::category` do not exist.

- [ ] **Step 3: Implement minimal category model**

Create:

```java
package dev.modernjava.upgrade.core;

public enum FindingCategory {
    RISK,
    BUILD,
    FRAMEWORK,
    LANGUAGE,
    CONCURRENCY,
    PERFORMANCE,
    OBSERVABILITY,
    AUTOMATION,
    BASELINE
}
```

Extend `Finding` constructor with `FindingCategory category` and update default rules.

- [ ] **Step 4: Run analyzer-core tests and verify GREEN**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl analyzer-core test
```

Expected: analyzer-core tests pass.

- [ ] **Step 5: Commit**

```bash
git add analyzer-core
git commit -m "feat: categorize migration findings"
```

### Task 2: Group Markdown Report Sections

**Files:**
- Modify: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/MarkdownReportRenderer.java`
- Test: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`
- Modify: `reports/sample-spring-boot-2-java-8-to-java-21.md`

- [ ] **Step 1: Write failing renderer tests**

Update expected Markdown to use `## Project Summary`, `## Migration Risks`, `## Framework Compatibility`, and `## Automation Suggestions`.

- [ ] **Step 2: Run renderer tests and verify RED**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl analyzer-core -Dtest=MarkdownReportRendererTest test
```

Expected: assertion failures because current renderer still emits `## Summary` and `## Findings`.

- [ ] **Step 3: Implement grouped rendering**

Use a fixed ordered mapping from categories to report section titles. Omit empty sections.

- [ ] **Step 4: Run renderer tests and verify GREEN**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl analyzer-core -Dtest=MarkdownReportRendererTest test
```

Expected: renderer tests pass.

- [ ] **Step 5: Commit**

```bash
git add analyzer-core reports
git commit -m "feat: group migration report sections"
```

### Task 3: Package CLI As Runnable Jar

**Files:**
- Modify: `cli/pom.xml`
- Test: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`
- Modify: `README.md`

- [ ] **Step 1: Write failing packaging expectation**

Add a test or verification step that expects `cli/target/modern-java-upgrade-lab-cli.jar` to exist after `mvn -pl cli -am package`.

- [ ] **Step 2: Run package and verify RED**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl cli -am package
```

Expected: package succeeds but no self-contained jar with the target name exists.

- [ ] **Step 3: Configure Maven Shade**

Add `maven-shade-plugin` in `cli/pom.xml` with main class `dev.modernjava.upgrade.cli.ModernJavaUpgradeLabApp` and final name `modern-java-upgrade-lab-cli`.

- [ ] **Step 4: Run package and jar smoke test**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl cli -am package
java -jar cli\target\modern-java-upgrade-lab-cli.jar analyze --path examples\spring-boot-2-java-8 --target 21
```

Expected: jar prints a Markdown report.

- [ ] **Step 5: Commit**

```bash
git add cli README.md
git commit -m "build: package runnable CLI jar"
```

### Task 4: Document Contributor Extension Path

**Files:**
- Create: `docs/contributing-rules.md`
- Modify: `CONTRIBUTING.md`
- Modify: `docs/bitacora/000-index.md`
- Create: `docs/bitacora/007-cli-usable-y-reportes-extensibles.md`

- [ ] **Step 1: Document how to add a rule**

Explain the `MigrationRule` shape, required tests, finding categories, and what not to automate yet.

- [ ] **Step 2: Update bitacora**

Write the first-person entry covering why CLI packaging and extensible report sections came before UI or JavaParser.

- [ ] **Step 3: Run full tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn test
```

Expected: full reactor passes.

- [ ] **Step 4: Commit**

```bash
git add docs CONTRIBUTING.md
git commit -m "docs: explain rule contribution path"
```
