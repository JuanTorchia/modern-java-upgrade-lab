# Modern Java Upgrade Lab MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first credible MVP of Modern Java Upgrade Lab: a Java 25 Maven multi-module CLI that analyzes a Maven/Spring Boot project and generates a Markdown migration readiness report.

**Architecture:** The MVP uses a small modular architecture: `cli` owns command-line interaction, `analyzer-core` owns the domain model and analysis orchestration, `build-inspectors` reads Maven project metadata, and `rewrite-adapter` suggests OpenRewrite recipes. Documentation and bitacora entries are treated as part of the deliverable.

**Tech Stack:** Java 25, Maven multi-module, Picocli, JUnit 5, AssertJ, Maven Model APIs, Markdown report rendering, GitHub issue templates.

---

## File Structure

- Create: `README.md`  
  Public project entry point with positioning, quickstart, MVP status, and contribution paths.
- Create: `LICENSE`  
  Apache 2.0 license.
- Create: `CONTRIBUTING.md`  
  Contribution guide focused on rules, fixtures, reports, and docs.
- Create: `CODE_OF_CONDUCT.md`  
  Contributor Covenant style community baseline.
- Create: `.gitignore`  
  Java/Maven/editor ignores.
- Create: `.github/ISSUE_TEMPLATE/feature_request.md`  
  Template for feature ideas.
- Create: `.github/ISSUE_TEMPLATE/rule_request.md`  
  Template for migration rule proposals.
- Create: `.github/ISSUE_TEMPLATE/good_first_issue.md`  
  Template for contributor-friendly tasks.
- Create: `docs/vision.md`  
  Product vision and community positioning.
- Create: `docs/roadmap.md`  
  90-day roadmap from the design spec.
- Create: `docs/blog-ideas.md`  
  Blog and talk content backlog.
- Create: `docs/bitacora/003-plan-de-implementacion.md`  
  First-person log for this planning step.
- Create: `pom.xml`  
  Root Maven parent with modules and dependency management.
- Create: `cli/pom.xml`
- Create: `cli/src/main/java/dev/modernjava/upgrade/cli/ModernJavaUpgradeLabApp.java`
- Create: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`
- Create: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`
- Create: `analyzer-core/pom.xml`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisRequest.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisResult.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Analyzer.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Finding.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/FindingSeverity.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/ProjectMetadata.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/MarkdownReportRenderer.java`
- Create: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`
- Create: `build-inspectors/pom.xml`
- Create: `build-inspectors/src/main/java/dev/modernjava/upgrade/build/MavenProjectInspector.java`
- Create: `build-inspectors/src/test/java/dev/modernjava/upgrade/build/MavenProjectInspectorTest.java`
- Create: `build-inspectors/src/test/resources/fixtures/maven-java8-springboot2/pom.xml`
- Create: `rewrite-adapter/pom.xml`
- Create: `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestion.java`
- Create: `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionService.java`
- Create: `rewrite-adapter/src/test/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionServiceTest.java`
- Create: `examples/spring-boot-2-java-8/pom.xml`
- Create: `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/DemoApplication.java`
- Create: `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/LegacyGreetingController.java`
- Create: `reports/sample-spring-boot-2-java-8-to-java-21.md`

## Task 1: Repository Community Foundation

**Files:**
- Create: `README.md`
- Create: `LICENSE`
- Create: `CONTRIBUTING.md`
- Create: `CODE_OF_CONDUCT.md`
- Create: `.gitignore`
- Create: `.github/ISSUE_TEMPLATE/feature_request.md`
- Create: `.github/ISSUE_TEMPLATE/rule_request.md`
- Create: `.github/ISSUE_TEMPLATE/good_first_issue.md`
- Create: `docs/vision.md`
- Create: `docs/roadmap.md`
- Create: `docs/blog-ideas.md`
- Create: `docs/bitacora/003-plan-de-implementacion.md`

- [ ] **Step 1: Create public project README**

Create `README.md`:

```markdown
# Modern Java Upgrade Lab

Evidence-based Java LTS migration reports for teams moving from Java 8/11/17/21 to modern Java.

Modern Java Upgrade Lab is an open source CLI and educational lab for understanding Java modernization across LTS releases. The goal is not to list new Java features. The goal is to analyze real projects, identify migration risks, suggest practical next steps, and create reproducible material for teams, blogs, and talks.

## MVP Focus

The first MVP focuses on Maven + Spring Boot projects and generates Markdown reports for target Java LTS versions.

```bash
mjul analyze --path . --target 21
```

The report answers:

- what Java version the project declares;
- what build tool and framework were detected;
- what blocks or risks the migration;
- what OpenRewrite recipes may help;
- what modernization opportunities are worth considering;
- what should not be modernized yet.

## Why This Exists

Java migrations are rarely just about syntax. Real teams deal with build plugins, framework compatibility, dependencies, CI behavior, runtime flags, observability, and test confidence.

This project treats Java modernization as an evidence-based migration path across LTS generations.

## Current Status

This repository is in early MVP development. The first target is a CLI that can analyze the included Spring Boot Java 8 example and produce a readable Markdown migration report.

## Project Principles

- Be useful before being flashy.
- Prefer honest diagnostics over automatic magic.
- Integrate with OpenRewrite where it helps.
- Keep examples reproducible.
- Document decisions in the bitacora.
- Make contribution paths small and clear.

## Repository Map

- `cli/` - command-line application.
- `analyzer-core/` - analysis model, findings, and report rendering.
- `build-inspectors/` - Maven and future Gradle project inspection.
- `rewrite-adapter/` - OpenRewrite recipe suggestions.
- `examples/` - reproducible migration examples.
- `reports/` - sample generated reports.
- `docs/` - vision, roadmap, blog ideas, talks, and bitacora.

## Contributing

Good first contributions include:

- adding a migration rule;
- improving a report explanation;
- adding a fixture project;
- documenting an OpenRewrite recipe;
- testing the analyzer against a real open source project.

See [CONTRIBUTING.md](CONTRIBUTING.md).
```

- [ ] **Step 2: Add Apache 2.0 license**

Create `LICENSE` with the standard Apache License 2.0 text from <https://www.apache.org/licenses/LICENSE-2.0.txt>.

- [ ] **Step 3: Add contribution guide**

Create `CONTRIBUTING.md`:

```markdown
# Contributing

Thanks for helping build Modern Java Upgrade Lab.

This project is designed for community impact. Contributions should make Java migration easier to understand, measure, explain, or execute.

## Good Contribution Areas

- Migration rules for Java LTS jumps.
- Maven or Gradle detection improvements.
- Spring Boot compatibility findings.
- OpenRewrite recipe documentation.
- Reproducible example projects.
- Clearer report language.
- Blog or talk material derived from real findings.

## Rule Contribution Checklist

When adding a rule, include:

- the migration path it applies to;
- the evidence the analyzer should look for;
- why it matters;
- the severity;
- the recommendation;
- at least one fixture or test.

## Writing Style

Be practical and honest. Do not present modern Java features as universal upgrades. Explain when a recommendation is optional or context-dependent.

## Development

The tool is built with Java 25 and Maven.

```bash
mvn test
```

## Bitacora

Project decisions are recorded under `docs/bitacora/`. If a contribution changes project direction, add or update a bitacora entry.
```

- [ ] **Step 4: Add code of conduct**

Create `CODE_OF_CONDUCT.md` with a concise community code:

```markdown
# Code of Conduct

Modern Java Upgrade Lab aims to be a respectful, practical, and welcoming Java community project.

Participants are expected to:

- be kind and constructive;
- assume good intent;
- critique ideas and code, not people;
- explain trade-offs clearly;
- help newcomers find useful first contributions.

Unacceptable behavior includes harassment, personal attacks, discriminatory language, and repeated disruptive behavior.

Maintainers may remove comments, close issues, or block participants who violate these expectations.
```

- [ ] **Step 5: Add `.gitignore`**

Create `.gitignore`:

```gitignore
target/
.idea/
.vscode/
*.iml
.classpath
.project
.settings/
.DS_Store
*.log
```

- [ ] **Step 6: Add issue templates**

Create `.github/ISSUE_TEMPLATE/feature_request.md`:

```markdown
---
name: Feature request
about: Suggest a product or CLI improvement
title: "feat: "
labels: enhancement
---

## Problem

What migration or learning problem should this solve?

## Proposed Behavior

What should the tool or documentation do?

## Target Users

Who benefits from this?

## Notes

Links, examples, or related tools.
```

Create `.github/ISSUE_TEMPLATE/rule_request.md`:

```markdown
---
name: Migration rule request
about: Propose a new analyzer rule or finding
title: "rule: "
labels: rule
---

## Migration Path

Example: Java 8 -> Java 17.

## Evidence To Detect

What should the analyzer look for?

## Why It Matters

What risk or opportunity does this represent?

## Recommendation

What should the report tell the user?
```

Create `.github/ISSUE_TEMPLATE/good_first_issue.md`:

```markdown
---
name: Good first issue
about: Small, well-scoped contribution
title: "good first issue: "
labels: good first issue
---

## Goal

What should be changed?

## Files

Which files are likely involved?

## Acceptance Criteria

- [ ] A focused change is implemented.
- [ ] Tests or docs are updated.
- [ ] The behavior is easy to understand from the PR.
```

- [ ] **Step 7: Add project docs**

Create `docs/vision.md`:

```markdown
# Vision

Modern Java Upgrade Lab helps Java teams migrate across LTS releases with evidence.

The project exists because Java modernization is larger than language features. It includes build tools, frameworks, dependencies, runtime behavior, observability, migration recipes, and team confidence.

The project should become a shared community lab where developers can analyze projects, compare migration paths, learn from examples, and contribute practical rules.
```

Create `docs/roadmap.md`:

```markdown
# Roadmap

## Days 1-15

- Create repository foundation.
- Build the Maven multi-module skeleton.
- Add the CLI entry point.
- Generate a basic Markdown report.
- Detect Maven and Java version.

## Days 16-30

- Detect Spring Boot version.
- Add first migration findings.
- Add the Spring Boot Java 8 example.
- Commit the first sample report.

## Days 31-45

- Add Java 8 -> 17 rules.
- Add Java 17 -> 21 rules.
- Suggest OpenRewrite recipes.

## Days 46-60

- Add Java 21 -> 25 rules.
- Improve report language.
- Add contributor docs for rules and fixtures.

## Days 61-90

- Add JavaParser source pattern detection.
- Add a small JFR lab.
- Test against small open source projects.
- Prepare release 0.1.0.
```

Create `docs/blog-ideas.md`:

```markdown
# Blog And Talk Ideas

- Migrating Java is not about syntax.
- Java 8 to modern LTS: what actually breaks first.
- Why the first output of this project is a Markdown report.
- OpenRewrite is powerful, but migration still needs diagnosis.
- Virtual threads should not be your first migration recommendation.
- Building Modern Java Upgrade Lab in public.
- How to read a Java migration readiness report.
```

- [ ] **Step 8: Add bitacora entry for planning**

Create `docs/bitacora/003-plan-de-implementacion.md`:

```markdown
# 003 - Plan de implementacion

## Fecha

2026-04-20

## Objetivo Del Paso

Queria convertir el diseno del MVP en un plan ejecutable paso a paso.

## Que Decidi

Decidi que el primer avance de codigo debe crear una base open source completa: README, licencia, guias de contribucion, estructura Maven multi-module, CLI minima, analyzer core, inspector Maven, sugerencias OpenRewrite, ejemplo Spring Boot y reporte sample.

## Que Descarte

Descarte empezar directamente por reglas complejas o analisis de codigo fuente. Antes necesito que el proyecto pueda correr, analizar un ejemplo simple y producir un reporte claro.

## Por Que

Si quiero generar impacto en la comunidad, el primer contacto con el repo importa. El proyecto tiene que explicar que problema resuelve, como se prueba y como se puede contribuir.

## Que Aprendi

Un buen MVP open source no empieza solo con codigo. Tambien empieza con una promesa clara, documentacion honesta y caminos de contribucion pequenos.

## Resultado Concreto

Quedo definido un plan de implementacion con tareas chicas, archivos exactos, comandos de prueba y criterios de aceptacion.

## Como Lo Contaria En Un Blog

Despues de validar la idea, no quise saltar directo a programar detectores. Primero arme el proyecto como si alguien fuera a verlo desde afuera: que problema resuelve, por que existe, como se prueba y donde puede ayudar la comunidad.

Ese fue el primer cambio de mentalidad: construir una herramienta y, al mismo tiempo, construir el contexto para que otros quieran participar.

## Proximo Paso

Ejecutar el scaffold inicial del repositorio y dejar corriendo la primera prueba Maven.
```

- [ ] **Step 9: Verify docs-only foundation**

Run:

```bash
git status --short
```

Expected: newly created documentation and metadata files are listed.

- [ ] **Step 10: Commit foundation**

Run:

```bash
git add README.md LICENSE CONTRIBUTING.md CODE_OF_CONDUCT.md .gitignore .github docs
git commit -m "docs: add open source project foundation"
```

Expected: commit succeeds.

## Task 2: Maven Multi-Module Skeleton

**Files:**
- Create: `pom.xml`
- Create: `cli/pom.xml`
- Create: `analyzer-core/pom.xml`
- Create: `build-inspectors/pom.xml`
- Create: `rewrite-adapter/pom.xml`

- [ ] **Step 1: Create root Maven parent**

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>dev.modernjava.upgrade</groupId>
    <artifactId>modern-java-upgrade-lab</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Modern Java Upgrade Lab</name>
    <description>Evidence-based Java LTS migration reports.</description>
    <url>https://github.com/modern-java-upgrade-lab/modern-java-upgrade-lab</url>

    <modules>
        <module>analyzer-core</module>
        <module>build-inspectors</module>
        <module>rewrite-adapter</module>
        <module>cli</module>
    </modules>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.3</junit.version>
        <assertj.version>3.26.3</assertj.version>
        <picocli.version>4.7.6</picocli.version>
        <maven.model.version>3.9.9</maven.model.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>${junit.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.13.0</version>
                    <configuration>
                        <release>${maven.compiler.release}</release>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.2.5</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: Create analyzer-core module POM**

Create `analyzer-core/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>dev.modernjava.upgrade</groupId>
        <artifactId>modern-java-upgrade-lab</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>analyzer-core</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3: Create build-inspectors module POM**

Create `build-inspectors/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>dev.modernjava.upgrade</groupId>
        <artifactId>modern-java-upgrade-lab</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>build-inspectors</artifactId>

    <dependencies>
        <dependency>
            <groupId>dev.modernjava.upgrade</groupId>
            <artifactId>analyzer-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-model</artifactId>
            <version>${maven.model.version}</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: Create rewrite-adapter module POM**

Create `rewrite-adapter/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>dev.modernjava.upgrade</groupId>
        <artifactId>modern-java-upgrade-lab</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>rewrite-adapter</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: Create CLI module POM**

Create `cli/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>dev.modernjava.upgrade</groupId>
        <artifactId>modern-java-upgrade-lab</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>cli</artifactId>

    <dependencies>
        <dependency>
            <groupId>dev.modernjava.upgrade</groupId>
            <artifactId>analyzer-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>dev.modernjava.upgrade</groupId>
            <artifactId>build-inspectors</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>dev.modernjava.upgrade</groupId>
            <artifactId>rewrite-adapter</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>info.picocli</groupId>
            <artifactId>picocli</artifactId>
            <version>${picocli.version}</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: Run Maven validation**

Run:

```bash
mvn validate
```

Expected: build success with all four modules discovered.

- [ ] **Step 7: Commit Maven skeleton**

Run:

```bash
git add pom.xml cli/pom.xml analyzer-core/pom.xml build-inspectors/pom.xml rewrite-adapter/pom.xml
git commit -m "build: add Maven multi-module skeleton"
```

Expected: commit succeeds.

## Task 3: Core Analysis Model And Markdown Report

**Files:**
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisRequest.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisResult.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Finding.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/FindingSeverity.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/ProjectMetadata.java`
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/MarkdownReportRenderer.java`
- Create: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`

- [ ] **Step 1: Write failing renderer test**

Create `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`:

```java
package dev.modernjava.upgrade.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownReportRendererTest {

    @Test
    void rendersProjectMetadataAndFindings() {
        var result = new AnalysisResult(
                new ProjectMetadata("maven", "8", "2.7.18", List.of("org.springframework.boot:spring-boot-starter-web")),
                21,
                List.of(new Finding(
                        "spring-boot-2-java-21-risk",
                        FindingSeverity.RISK,
                        "Spring Boot compatibility",
                        "Spring Boot 2.x is not the best baseline for Java 21",
                        "Detected Spring Boot 2.7.18",
                        "Plan a Spring Boot 3.x migration before relying on Java 21 in production.",
                        "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2"
                ))
        );

        String markdown = new MarkdownReportRenderer().render(new AnalysisRequest(Path.of("."), 21), result);

        assertThat(markdown).contains("# Modern Java Upgrade Report");
        assertThat(markdown).contains("Build tool: Maven");
        assertThat(markdown).contains("Declared Java version: 8");
        assertThat(markdown).contains("Target Java version: 21");
        assertThat(markdown).contains("Spring Boot 2.x is not the best baseline for Java 21");
        assertThat(markdown).contains("OpenRewrite recipe");
    }
}
```

- [ ] **Step 2: Run failing test**

Run:

```bash
mvn -pl analyzer-core test
```

Expected: FAIL because core classes do not exist.

- [ ] **Step 3: Implement core records and enum**

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisRequest.java`:

```java
package dev.modernjava.upgrade.core;

import java.nio.file.Path;

public record AnalysisRequest(Path projectPath, int targetJavaVersion) {
}
```

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/ProjectMetadata.java`:

```java
package dev.modernjava.upgrade.core;

import java.util.List;

public record ProjectMetadata(
        String buildTool,
        String declaredJavaVersion,
        String springBootVersion,
        List<String> dependencies
) {
}
```

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/FindingSeverity.java`:

```java
package dev.modernjava.upgrade.core;

public enum FindingSeverity {
    BLOCKER,
    RISK,
    OPPORTUNITY,
    INFO
}
```

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Finding.java`:

```java
package dev.modernjava.upgrade.core;

public record Finding(
        String id,
        FindingSeverity severity,
        String area,
        String title,
        String evidence,
        String recommendation,
        String openRewriteRecipe
) {
}
```

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/AnalysisResult.java`:

```java
package dev.modernjava.upgrade.core;

import java.util.List;

public record AnalysisResult(ProjectMetadata metadata, int targetJavaVersion, List<Finding> findings) {
}
```

- [ ] **Step 4: Implement Markdown renderer**

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/MarkdownReportRenderer.java`:

```java
package dev.modernjava.upgrade.core;

import java.util.Locale;

public final class MarkdownReportRenderer {

    public String render(AnalysisRequest request, AnalysisResult result) {
        var metadata = result.metadata();
        var report = new StringBuilder();

        report.append("# Modern Java Upgrade Report\n\n");
        report.append("Project path: `").append(request.projectPath().toAbsolutePath().normalize()).append("`\n\n");
        report.append("## Summary\n\n");
        report.append("- Build tool: ").append(display(metadata.buildTool())).append("\n");
        report.append("- Declared Java version: ").append(valueOrUnknown(metadata.declaredJavaVersion())).append("\n");
        report.append("- Target Java version: ").append(result.targetJavaVersion()).append("\n");
        report.append("- Spring Boot version: ").append(valueOrUnknown(metadata.springBootVersion())).append("\n\n");

        report.append("## Findings\n\n");
        if (result.findings().isEmpty()) {
            report.append("No findings were generated yet.\n");
            return report.toString();
        }

        for (Finding finding : result.findings()) {
            report.append("### [").append(finding.severity()).append("] ").append(finding.title()).append("\n\n");
            report.append("- Area: ").append(finding.area()).append("\n");
            report.append("- Evidence: ").append(finding.evidence()).append("\n");
            report.append("- Recommendation: ").append(finding.recommendation()).append("\n");
            if (finding.openRewriteRecipe() != null && !finding.openRewriteRecipe().isBlank()) {
                report.append("- OpenRewrite recipe: `").append(finding.openRewriteRecipe()).append("`\n");
            }
            report.append("\n");
        }

        return report.toString();
    }

    private String display(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "Unknown" : value;
    }
}
```

- [ ] **Step 5: Run renderer test**

Run:

```bash
mvn -pl analyzer-core test
```

Expected: PASS.

- [ ] **Step 6: Commit core model**

Run:

```bash
git add analyzer-core
git commit -m "feat: add core analysis report model"
```

Expected: commit succeeds.

## Task 4: Maven Project Inspector

**Files:**
- Create: `build-inspectors/src/main/java/dev/modernjava/upgrade/build/MavenProjectInspector.java`
- Create: `build-inspectors/src/test/java/dev/modernjava/upgrade/build/MavenProjectInspectorTest.java`
- Create: `build-inspectors/src/test/resources/fixtures/maven-java8-springboot2/pom.xml`

- [ ] **Step 1: Write Maven fixture**

Create `build-inspectors/src/test/resources/fixtures/maven-java8-springboot2/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>dev.modernjava.upgrade</groupId>
    <artifactId>fixture-java8-springboot2</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write failing inspector test**

Create `build-inspectors/src/test/java/dev/modernjava/upgrade/build/MavenProjectInspectorTest.java`:

```java
package dev.modernjava.upgrade.build;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MavenProjectInspectorTest {

    @Test
    void readsJavaVersionSpringBootVersionAndDependencies() {
        var project = Path.of("src/test/resources/fixtures/maven-java8-springboot2");

        var metadata = new MavenProjectInspector().inspect(project);

        assertThat(metadata.buildTool()).isEqualTo("maven");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("8");
        assertThat(metadata.springBootVersion()).isEqualTo("2.7.18");
        assertThat(metadata.dependencies()).contains("org.springframework.boot:spring-boot-starter-web");
    }
}
```

- [ ] **Step 3: Run failing inspector test**

Run:

```bash
mvn -pl build-inspectors test
```

Expected: FAIL because `MavenProjectInspector` does not exist.

- [ ] **Step 4: Implement Maven inspector**

Create `build-inspectors/src/main/java/dev/modernjava/upgrade/build/MavenProjectInspector.java`:

```java
package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.ProjectMetadata;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public final class MavenProjectInspector {

    public ProjectMetadata inspect(Path projectPath) {
        Path pom = projectPath.resolve("pom.xml");
        if (!Files.exists(pom)) {
            throw new IllegalArgumentException("No pom.xml found at " + projectPath.toAbsolutePath().normalize());
        }

        Model model = readModel(pom);
        String javaVersion = normalizeJavaVersion(findJavaVersion(model));
        String springBootVersion = findSpringBootVersion(model);
        List<String> dependencies = model.getDependencies().stream()
                .map(this::coordinates)
                .toList();

        return new ProjectMetadata("maven", javaVersion, springBootVersion, dependencies);
    }

    private Model readModel(Path pom) {
        try (Reader reader = Files.newBufferedReader(pom)) {
            return new MavenXpp3Reader().read(reader);
        } catch (IOException | org.codehaus.plexus.util.xml.pull.XmlPullParserException exception) {
            throw new IllegalArgumentException("Could not read Maven model from " + pom, exception);
        }
    }

    private String findJavaVersion(Model model) {
        Properties properties = model.getProperties();
        String javaVersion = properties.getProperty("java.version");
        if (javaVersion != null) {
            return javaVersion;
        }

        String compilerRelease = properties.getProperty("maven.compiler.release");
        if (compilerRelease != null) {
            return compilerRelease;
        }

        String compilerTarget = properties.getProperty("maven.compiler.target");
        if (compilerTarget != null) {
            return compilerTarget;
        }

        return null;
    }

    private String normalizeJavaVersion(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.equals("1.8") ? "8" : value;
    }

    private String findSpringBootVersion(Model model) {
        Parent parent = model.getParent();
        if (parent != null && "org.springframework.boot".equals(parent.getGroupId())) {
            return parent.getVersion();
        }
        return model.getDependencies().stream()
                .filter(dependency -> "org.springframework.boot".equals(dependency.getGroupId()))
                .map(Dependency::getVersion)
                .filter(version -> version != null && !version.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String coordinates(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId();
    }
}
```

- [ ] **Step 5: Run inspector test**

Run:

```bash
mvn -pl build-inspectors test
```

Expected: PASS.

- [ ] **Step 6: Commit Maven inspector**

Run:

```bash
git add build-inspectors
git commit -m "feat: inspect Maven project metadata"
```

Expected: commit succeeds.

## Task 5: OpenRewrite Suggestions

**Files:**
- Create: `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestion.java`
- Create: `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionService.java`
- Create: `rewrite-adapter/src/test/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionServiceTest.java`

- [ ] **Step 1: Write failing OpenRewrite suggestion test**

Create `rewrite-adapter/src/test/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionServiceTest.java`:

```java
package dev.modernjava.upgrade.rewrite;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRewriteSuggestionServiceTest {

    @Test
    void suggestsJavaMigrationRecipeForTargetVersion() {
        var suggestions = new OpenRewriteSuggestionService().suggestForTarget(21);

        assertThat(suggestions)
                .extracting(OpenRewriteSuggestion::recipe)
                .contains("org.openrewrite.java.migrate.UpgradeToJava21");
    }

    @Test
    void rendersMavenCommand() {
        var suggestion = new OpenRewriteSuggestion(
                "Upgrade Java to 21",
                "org.openrewrite.java.migrate.UpgradeToJava21"
        );

        assertThat(suggestion.mavenCommand()).contains("rewrite-maven-plugin:run");
        assertThat(suggestion.mavenCommand()).contains("org.openrewrite.java.migrate.UpgradeToJava21");
    }
}
```

- [ ] **Step 2: Run failing OpenRewrite tests**

Run:

```bash
mvn -pl rewrite-adapter test
```

Expected: FAIL because OpenRewrite classes do not exist.

- [ ] **Step 3: Implement OpenRewrite suggestion model**

Create `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestion.java`:

```java
package dev.modernjava.upgrade.rewrite;

public record OpenRewriteSuggestion(String title, String recipe) {

    public String mavenCommand() {
        return "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run "
                + "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE "
                + "-Drewrite.activeRecipes=" + recipe
                + " -Drewrite.exportDatatables=true";
    }
}
```

Create `rewrite-adapter/src/main/java/dev/modernjava/upgrade/rewrite/OpenRewriteSuggestionService.java`:

```java
package dev.modernjava.upgrade.rewrite;

import java.util.List;

public final class OpenRewriteSuggestionService {

    public List<OpenRewriteSuggestion> suggestForTarget(int targetJavaVersion) {
        return switch (targetJavaVersion) {
            case 17 -> List.of(new OpenRewriteSuggestion("Upgrade Java to 17", "org.openrewrite.java.migrate.UpgradeToJava17"));
            case 21 -> List.of(new OpenRewriteSuggestion("Upgrade Java to 21", "org.openrewrite.java.migrate.UpgradeToJava21"));
            case 25 -> List.of(new OpenRewriteSuggestion("Upgrade Java to 25", "org.openrewrite.java.migrate.UpgradeToJava25"));
            default -> List.of();
        };
    }
}
```

- [ ] **Step 4: Run OpenRewrite tests**

Run:

```bash
mvn -pl rewrite-adapter test
```

Expected: PASS.

- [ ] **Step 5: Commit OpenRewrite suggestions**

Run:

```bash
git add rewrite-adapter
git commit -m "feat: suggest OpenRewrite migration recipes"
```

Expected: commit succeeds.

## Task 6: Analyzer Orchestration

**Files:**
- Create: `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Analyzer.java`
- Modify: `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`

- [ ] **Step 1: Add analyzer test in core**

Append this test to `analyzer-core/src/test/java/dev/modernjava/upgrade/core/MarkdownReportRendererTest.java`:

```java
    @Test
    void emptyResultStillRendersUsefulSummary() {
        var result = new AnalysisResult(
                new ProjectMetadata("maven", "17", null, List.of()),
                21,
                List.of()
        );

        String markdown = new MarkdownReportRenderer().render(new AnalysisRequest(Path.of("."), 21), result);

        assertThat(markdown).contains("No findings were generated yet.");
    }
```

- [ ] **Step 2: Create analyzer interface**

Create `analyzer-core/src/main/java/dev/modernjava/upgrade/core/Analyzer.java`:

```java
package dev.modernjava.upgrade.core;

public interface Analyzer {
    AnalysisResult analyze(AnalysisRequest request);
}
```

- [ ] **Step 3: Run analyzer-core tests**

Run:

```bash
mvn -pl analyzer-core test
```

Expected: PASS.

- [ ] **Step 4: Commit analyzer interface**

Run:

```bash
git add analyzer-core
git commit -m "feat: define analyzer contract"
```

Expected: commit succeeds.

## Task 7: CLI Analyze Command

**Files:**
- Create: `cli/src/main/java/dev/modernjava/upgrade/cli/ModernJavaUpgradeLabApp.java`
- Create: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`
- Create: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`

- [ ] **Step 1: Write failing CLI help test**

Create `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`:

```java
package dev.modernjava.upgrade.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeCommandTest {

    @Test
    void rootCommandIncludesAnalyzeSubcommand() {
        var output = new StringWriter();
        var commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output));

        int exitCode = commandLine.execute("--help");

        assertThat(exitCode).isZero();
        assertThat(output.toString()).contains("analyze");
    }
}
```

- [ ] **Step 2: Run failing CLI test**

Run:

```bash
mvn -pl cli test
```

Expected: FAIL because CLI classes do not exist.

- [ ] **Step 3: Implement CLI root command**

Create `cli/src/main/java/dev/modernjava/upgrade/cli/ModernJavaUpgradeLabApp.java`:

```java
package dev.modernjava.upgrade.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "mjul",
        mixinStandardHelpOptions = true,
        version = "mjul 0.1.0-SNAPSHOT",
        description = "Generate evidence-based Java LTS migration reports.",
        subcommands = AnalyzeCommand.class
)
public final class ModernJavaUpgradeLabApp implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ModernJavaUpgradeLabApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }
}
```

Create `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`:

```java
package dev.modernjava.upgrade.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report."
)
public final class AnalyzeCommand implements Callable<Integer> {

    @Option(names = "--path", description = "Project path to analyze.", defaultValue = ".")
    private Path path;

    @Option(names = "--target", description = "Target Java version.", required = true)
    private int target;

    @Override
    public Integer call() {
        System.out.println("# Modern Java Upgrade Report");
        System.out.println();
        System.out.println("Project path: `" + path.toAbsolutePath().normalize() + "`");
        System.out.println("Target Java version: " + target);
        System.out.println();
        System.out.println("Analyzer wiring will be connected in the next task.");
        return 0;
    }
}
```

- [ ] **Step 4: Run CLI tests**

Run:

```bash
mvn -pl cli test
```

Expected: PASS.

- [ ] **Step 5: Manually run CLI help**

Run:

```bash
mvn -pl cli exec:java -Dexec.mainClass=dev.modernjava.upgrade.cli.ModernJavaUpgradeLabApp -Dexec.args="--help"
```

Expected: command prints help. If `exec-maven-plugin` is missing, skip this manual command and rely on the test until packaging is added.

- [ ] **Step 6: Commit CLI skeleton**

Run:

```bash
git add cli
git commit -m "feat: add CLI analyze command skeleton"
```

Expected: commit succeeds.

## Task 8: Wire CLI To Maven Inspector And Report Renderer

**Files:**
- Modify: `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java`
- Modify: `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`

- [ ] **Step 1: Add CLI analyze output test**

Append this test to `cli/src/test/java/dev/modernjava/upgrade/cli/AnalyzeCommandTest.java`:

```java
    @Test
    void analyzeCommandPrintsMarkdownReportForMavenProject() {
        var output = new StringWriter();
        var commandLine = new CommandLine(new ModernJavaUpgradeLabApp());
        commandLine.setOut(new PrintWriter(output));

        int exitCode = commandLine.execute(
                "analyze",
                "--path", "../build-inspectors/src/test/resources/fixtures/maven-java8-springboot2",
                "--target", "21"
        );

        assertThat(exitCode).isZero();
        assertThat(output.toString()).contains("# Modern Java Upgrade Report");
        assertThat(output.toString()).contains("Declared Java version: 8");
        assertThat(output.toString()).contains("Spring Boot version: 2.7.18");
    }
```

- [ ] **Step 2: Run failing CLI wiring test**

Run:

```bash
mvn -pl cli test
```

Expected: FAIL because `AnalyzeCommand` writes directly to `System.out`, not Picocli output, and does not inspect Maven yet.

- [ ] **Step 3: Wire analyzer behavior into CLI command**

Replace `cli/src/main/java/dev/modernjava/upgrade/cli/AnalyzeCommand.java` with:

```java
package dev.modernjava.upgrade.cli;

import dev.modernjava.upgrade.build.MavenProjectInspector;
import dev.modernjava.upgrade.core.AnalysisRequest;
import dev.modernjava.upgrade.core.AnalysisResult;
import dev.modernjava.upgrade.core.Finding;
import dev.modernjava.upgrade.core.FindingSeverity;
import dev.modernjava.upgrade.core.MarkdownReportRenderer;
import dev.modernjava.upgrade.rewrite.OpenRewriteSuggestionService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "Analyze a Java project and print a migration readiness report."
)
public final class AnalyzeCommand implements Callable<Integer> {

    @Option(names = "--path", description = "Project path to analyze.", defaultValue = ".")
    private Path path;

    @Option(names = "--target", description = "Target Java version.", required = true)
    private int target;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        var request = new AnalysisRequest(path, target);
        var metadata = new MavenProjectInspector().inspect(path);
        var findings = new ArrayList<Finding>();

        if (target >= 21 && metadata.springBootVersion() != null && metadata.springBootVersion().startsWith("2.")) {
            findings.add(new Finding(
                    "spring-boot-2-java-21-risk",
                    FindingSeverity.RISK,
                    "Spring Boot compatibility",
                    "Spring Boot 2.x needs review before a Java " + target + " production migration",
                    "Detected Spring Boot " + metadata.springBootVersion(),
                    "Plan a Spring Boot 3.x migration path and validate framework compatibility before relying on Java " + target + ".",
                    "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2"
            ));
        }

        new OpenRewriteSuggestionService().suggestForTarget(target).stream()
                .findFirst()
                .ifPresent(suggestion -> findings.add(new Finding(
                        "openrewrite-java-" + target,
                        FindingSeverity.INFO,
                        "Migration automation",
                        "OpenRewrite has a Java " + target + " migration recipe",
                        "Target Java version is " + target,
                        "Review and run the suggested OpenRewrite recipe in a branch before manual changes.",
                        suggestion.recipe()
                )));

        var result = new AnalysisResult(metadata, target, findings);
        String markdown = new MarkdownReportRenderer().render(request, result);
        spec.commandLine().getOut().println(markdown);
        return 0;
    }
}
```

- [ ] **Step 4: Run CLI wiring test**

Run:

```bash
mvn -pl cli -am test
```

Expected: PASS across required modules.

- [ ] **Step 5: Commit CLI wiring**

Run:

```bash
git add cli
git commit -m "feat: generate report from Maven project analysis"
```

Expected: commit succeeds.

## Task 9: Example Project And Sample Report

**Files:**
- Create: `examples/spring-boot-2-java-8/pom.xml`
- Create: `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/DemoApplication.java`
- Create: `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/LegacyGreetingController.java`
- Create: `reports/sample-spring-boot-2-java-8-to-java-21.md`

- [ ] **Step 1: Create Spring Boot Java 8 example POM**

Create `examples/spring-boot-2-java-8/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>dev.modernjava.upgrade</groupId>
    <artifactId>spring-boot-2-java-8-example</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Create example application**

Create `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/DemoApplication.java`:

```java
package dev.modernjava.upgrade.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

Create `examples/spring-boot-2-java-8/src/main/java/dev/modernjava/upgrade/example/LegacyGreetingController.java`:

```java
package dev.modernjava.upgrade.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LegacyGreetingController {

    @GetMapping("/greeting")
    public Map<String, Object> greeting() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from a Java 8 style Spring Boot app");
        response.put("modernizationHint", "This response could later become a record-based DTO.");
        return response;
    }
}
```

- [ ] **Step 3: Run CLI against example**

Run:

```bash
mvn -pl cli -am test
```

Expected: PASS.

Then run:

```bash
java -cp cli/target/classes dev.modernjava.upgrade.cli.ModernJavaUpgradeLabApp analyze --path examples/spring-boot-2-java-8 --target 21
```

Expected: if the direct `java -cp` command fails because dependencies are not on the classpath, use the test output as verification until packaging is added.

- [ ] **Step 4: Create sample report manually from expected CLI output**

Create `reports/sample-spring-boot-2-java-8-to-java-21.md`:

```markdown
# Modern Java Upgrade Report

Project path: `examples/spring-boot-2-java-8`

## Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 21
- Spring Boot version: 2.7.18

## Findings

### [RISK] Spring Boot 2.x needs review before a Java 21 production migration

- Area: Spring Boot compatibility
- Evidence: Detected Spring Boot 2.7.18
- Recommendation: Plan a Spring Boot 3.x migration path and validate framework compatibility before relying on Java 21.
- OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
```

- [ ] **Step 5: Commit example and report**

Run:

```bash
git add examples reports
git commit -m "example: add Spring Boot Java 8 migration sample"
```

Expected: commit succeeds.

## Task 10: Final Verification And Bitacora Update

**Files:**
- Modify: `docs/bitacora/000-index.md`
- Create: `docs/bitacora/004-scaffold-del-repo.md`

- [ ] **Step 1: Add scaffold bitacora entry**

Create `docs/bitacora/004-scaffold-del-repo.md`:

```markdown
# 004 - Scaffold del repo

## Fecha

2026-04-20

## Objetivo Del Paso

Queria convertir el diseno y el plan en una primera version real del repositorio.

## Que Decidi

Decidi crear primero una base pequena pero completa: documentacion publica, estructura Maven multi-module, CLI, modelo de analisis, inspector Maven, sugerencias OpenRewrite, ejemplo Spring Boot y reporte sample.

## Que Descarte

No agregue dashboard, HTML report, Gradle profundo ni analisis avanzado de codigo fuente en este primer scaffold.

## Por Que

El objetivo era que el proyecto pudiera explicar su valor desde el primer dia y que alguien pudiera correr tests, leer el reporte sample y entender como contribuir.

## Que Aprendi

La estructura inicial de un proyecto open source tambien comunica estrategia. Si el repo empieza con ejemplos, reportes y bitacora, la comunidad entiende que el objetivo no es solo codigo sino aprendizaje reproducible.

## Resultado Concreto

El repositorio quedo con una CLI inicial, modulos Maven, tests, ejemplo Spring Boot Java 8 y un reporte sample para migracion hacia Java 21.

## Como Lo Contaria En Un Blog

El primer scaffold no intento resolver toda la migracion Java. Intento algo mas importante: crear una base creible para iterar.

En vez de empezar por features modernas, empece por el flujo minimo: analizar un proyecto, detectar informacion basica y producir un reporte que una persona pueda leer.

## Proximo Paso

Mejorar las reglas de migracion Java 8 -> 17 y convertir los findings iniciales en un pequeno catalogo extensible.
```

- [ ] **Step 2: Update bitacora index**

Modify `docs/bitacora/000-index.md` and add:

```markdown
- [003 - Plan de implementacion](003-plan-de-implementacion.md)
- [004 - Scaffold del repo](004-scaffold-del-repo.md)
```

Expected: index links to all entries.

- [ ] **Step 3: Run full test suite**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 4: Check Git status**

Run:

```bash
git status --short
```

Expected: only bitacora changes are uncommitted.

- [ ] **Step 5: Commit final bitacora update**

Run:

```bash
git add docs/bitacora
git commit -m "docs: document initial scaffold journey"
```

Expected: commit succeeds.

## Plan Self-Review

### Spec Coverage

- CLI-first MVP is covered by Tasks 2, 7, and 8.
- Maven detection is covered by Task 4.
- Spring Boot detection is covered by Task 4.
- Markdown report generation is covered by Task 3.
- OpenRewrite recipe suggestions are covered by Task 5.
- Spring Boot Java 8 example and sample report are covered by Task 9.
- Bitacora requirement is covered by Tasks 1 and 10.
- Community foundation is covered by Task 1.

### Placeholder Scan

This plan intentionally avoids open-ended placeholders. Known future work such as Gradle support, HTML reports, JavaParser source scanning, and JFR labs is excluded from MVP and belongs in later plans.

### Type Consistency

The package prefix is consistently `dev.modernjava.upgrade`. The CLI command is consistently `mjul analyze --path . --target 21`. The core domain records match the renderer and CLI wiring tasks.
