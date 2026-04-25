# Open Source Reference Cases

This document defines how Modern Java Upgrade Lab should use real open source Java projects as adoption material, validation fixtures, and technical content.

## Goal

Use real repositories to prove that the analyzer produces useful, reviewable migration reports.

The first goal is not broad benchmarking. It is to build a small, credible catalog of reproducible migration cases that Java engineers can inspect, challenge, and share.

## Selection Criteria

A good reference case should:

- use Maven or Gradle;
- contain a Spring Boot application or a realistic JVM service;
- declare or imply a Java baseline that the analyzer can detect;
- be small enough that the generated report can be reviewed end to end;
- have a stable branch, tag, or commit that can be pinned;
- avoid requiring external credentials for static analysis;
- expose useful migration signals in build files, dependencies, plugins, or source code.

Avoid, for the first pass:

- very large framework repositories;
- multi-hundred-module platforms;
- repositories that require private services or cloud credentials to inspect;
- projects where the build layout is so unusual that the first case study becomes a tool-debugging exercise.

## Recommended First Set

### 1. Spring PetClinic

- Repository: https://github.com/spring-projects/spring-petclinic
- Why: canonical Spring sample with Maven and Gradle build files.
- Current baseline: README states Java 17 or newer is required.
- Best use: Java 17 to Java 21 or Java 25 report quality; Maven vs Gradle build detection.
- Initial priority: high.

This should be the first public case because readers recognize it and the report should be easy to understand.

### 2. JHipster Sample App

- Repository: https://github.com/jhipster/jhipster-sample-app
- Why: generated real-world-style Spring Boot application with more production-like dependencies.
- Best use: dependency baseline, framework baseline, and report readability against a busier application.
- Initial priority: high.

Use a pinned tag or commit. JHipster evolves quickly, so the report must not track a moving branch without recording the exact commit.

### 3. RealWorld Spring Boot Java

- Repository: https://github.com/raeperd/realworld-springboot-java
- Why: RealWorld-style backend implementation with Spring Boot, security, JPA, and API behavior.
- Best use: older Spring Boot / Java baseline exploration, depending on the selected tag.
- Initial priority: medium.

This is useful if it exposes older Java or Spring Boot signals, but it should be validated before becoming a public case study.

### 4. Gothinkster Spring Boot RealWorld Example

- Repository: https://github.com/gothinkster/spring-boot-realworld-example-app
- Why: recognizable RealWorld API implementation.
- Best use: legacy baseline detection and false-positive discovery.
- Initial priority: medium.

Treat this as a validation fixture first. Only publish a case study if the report is clear and technically fair.

### 5. Okta Java Microservices Examples

- Repository: https://github.com/oktadev/java-microservices-examples
- Why: microservices examples with Spring Boot, Spring Cloud, JHipster, Maven, and Gradle subprojects.
- Current baseline: README lists Java 11 as a prerequisite.
- Best use: multi-project analysis and Java 11 to Java 21 report behavior.
- Initial priority: medium.

Do static analysis only. Do not require Okta credentials or running services for the first pass.

## Reproducible Workflow

Each case study must pin the repository state before generating a report.

```powershell
git clone <repo-url> .worktrees/reference-cases/<case-name>
cd .worktrees/reference-cases/<case-name>
git checkout <tag-or-commit>
```

Build the CLI from this repository:

```powershell
mvn -pl cli -am package
```

Generate a report:

```powershell
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze `
  --path .worktrees/reference-cases/<case-name> `
  --target 21 `
  --output reports/reference-<case-name>-to-java-21.md
```

For projects already on Java 17 or newer, prefer a Java 21 or Java 25 target:

```powershell
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze `
  --path .worktrees/reference-cases/<case-name> `
  --target 25 `
  --output reports/reference-<case-name>-to-java-25.md
```

## Case Study Template

Each public case study should use this structure:

```markdown
# <Project> to Java <Target>

- Repository: <url>
- Commit/tag: <sha-or-tag>
- Analyzed on: <date>
- Build tool detected: <Maven/Gradle>
- Declared Java baseline: <version or unknown>
- Target Java: <version>

## Why This Project

## Analyzer Findings

## Useful Signals

## False Positives Or Gaps

## What The Report Helps A Team Decide

## Next Analyzer Improvements
```

## Adoption Loop

For each reference project:

1. Generate the report.
2. Read it as if preparing a migration planning meeting.
3. Record confusing, weak, or missing findings as analyzer issues.
4. Promote only the strongest reports into `docs/case-studies/`.
5. Turn each promoted case into a short technical post.

The case study should be honest about gaps. A report that exposes a missing rule is still valuable if it creates a concrete analyzer improvement.

## First Execution Order

Recommended order:

1. Spring PetClinic, target Java 25.
2. JHipster Sample App, target Java 25.
3. Okta Java Microservices Examples, target Java 21.
4. RealWorld Spring Boot Java, target Java 21.
5. Gothinkster Spring Boot RealWorld Example, target Java 21.

This order gives one polished recognizable case first, then progressively messier projects that can harden the analyzer.

## Current Generated Case Index

The first generated reports are tracked in [case-studies/index.md](case-studies/index.md).
