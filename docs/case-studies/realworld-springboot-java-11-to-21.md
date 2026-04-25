# RealWorld Spring Boot Java 11 to Java 21

This case study shows how Modern Java Upgrade Lab analyzes a real Spring Boot application before a Java LTS migration.

It is based on the RealWorld Spring Boot Java implementation, pinned to a specific commit so the result can be reproduced and reviewed.

## Source

- Repository: https://github.com/raeperd/realworld-springboot-java
- Commit: `56be3ced4f3134424ead5fcaf387b3aa640b9532`
- Branch at analysis time: `master`
- Build tool detected: Gradle
- Declared Java version: 11
- Target Java version: 21
- Spring Boot version detected: 2.5.2
- Generated report: [reference-realworld-springboot-java-to-java-21.md](../../reports/reference-realworld-springboot-java-to-java-21.md)
- Generated JSON: [reference-realworld-springboot-java-to-java-21.json](../../reports/reference-realworld-springboot-java-to-java-21.json)

## Why This Project

This project is a useful migration reference because it is not a toy "hello world" application, but it is still small enough for a report to be read end to end.

The baseline also represents a common real-world situation: a Java 11 application on Spring Boot 2.x where the target is Java 21. That migration is not just a compiler setting change. It requires framework compatibility review, test baseline work, and separation between runtime migration and optional source modernization.

## Analyzer Summary

The analyzer classified the migration as:

```text
Upgrade required (Java 11 -> 21)
```

It also assigned:

```text
Risk level: HIGH
Risk score: 90/100
```

It detected:

- Gradle as the build tool;
- Java 11 as the declared baseline;
- Spring Boot 2.5.2;
- Gradle wrapper 6.8.3;
- versioned build plugins including Spring Boot, dependency-management, Lombok, SonarQube, and Jib;
- the Jib runtime base image `openjdk:11.0.10-jre-buster`;
- a pinned Mockito test dependency;
- GitHub Actions as the detected CI provider;
- `./gradlew test` as the suggested baseline test command;
- Java 21 as the requested target;
- an applicable OpenRewrite Java 21 migration recipe.

The most important signal was not a syntax modernization suggestion. It was the framework compatibility risk.

## Key Finding

The report flagged Spring Boot 2.x as a migration risk for Java 21 planning:

```text
Spring Boot 2.x needs compatibility validation before a Java 21 migration
```

The recommendation is to validate the project on Spring Boot 2.7.x before rolling out Java 21, and to treat Spring Boot 3.x as a separate migration because of the Jakarta namespace transition.

That is the right shape of recommendation for this kind of project. A direct Java 11 to Java 21 runtime jump combined with a Spring Boot 2 to 3 framework migration would blend multiple sources of failure into one change set.

## Migration Plan Produced

The report generated a staged plan:

1. Establish the baseline: confirm Java version, run tests, make compiler and runtime configuration explicit.
2. Review framework compatibility: validate Spring Boot 2.5.2 before moving to Java 21.
3. Run automation in a branch: use OpenRewrite as reviewable automation, not as an unreviewed rewrite.
4. Defer optional modernization: keep DTO/record/language cleanup out of the baseline migration branch.
5. Roll out carefully: validate CI, container images, runtime flags, observability, and rollback paths.

This is the intended product posture: the tool does not claim to upgrade the application automatically. It creates an evidence-backed migration plan that a senior engineer can review.

## Dependency And Runtime Baselines

The generated report now includes a dedicated Dependency & Plugin Baselines section. For this project, that section captures:

- Gradle wrapper `6.8.3`;
- Spring Boot Gradle plugin `2.5.2`;
- dependency-management plugin `1.0.11.RELEASE`;
- Jib plugin `3.1.4`;
- Mockito inline `3.12.1`;
- Jib base image `openjdk:11.0.10-jre-buster`.

This matters for enterprise evaluation because migration planning is not only "which Java version?" A team also needs to know which build runner, framework plugin, test libraries, and container runtime baseline are part of the blast radius.

## Build Readiness And Work Items

The generated report now includes a Build Readiness section. For this project it detects:

- Gradle wrapper present;
- GitHub Actions workflow at `.github/workflows/build.yml`;
- suggested baseline command `./gradlew test`.

The report also produces recommended work items that can be copied into a backlog:

- run baseline tests in CI before migration changes;
- stage Spring Boot 2.7.x before the Java 21 rollout;
- validate or upgrade the Gradle wrapper before Java 21;
- replace the Java 11 runtime image;
- run the OpenRewrite Java 21 recipe in a branch.

This is more useful to a company than a passive finding list because it turns static analysis into migration planning units.

## Machine-Readable Output

The same analysis can now be emitted as JSON with:

```powershell
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/realworld-springboot-java --target 21 --format json --output reports/reference-realworld-springboot-java-to-java-21.json
```

That JSON includes `riskLevel`, `riskScore`, `riskReasons`, build readiness, project metadata, dependency baselines, work items, findings, and analysis metadata. This is the first step toward CI gates, dashboards, and portfolio-level migration tracking.

## What This Helps A Team Decide

The report gives a team enough evidence to split the migration into safer work items:

- create a Java 11 baseline report;
- make build and CI Java configuration explicit;
- validate the current Spring Boot 2.5.2 application;
- decide whether to move first to Spring Boot 2.7.x;
- run the Java 21 OpenRewrite recipe in a branch;
- keep Spring Boot 3 and Jakarta changes as a separate project.

That separation matters because it keeps failures attributable. If tests fail after changing Java, Spring Boot, Jakarta imports, dependencies, and source style all at once, the migration becomes harder to debug.

## What The Tool Does Not Claim

The report does not prove that the application runs on Java 21.

It does not execute Gradle, run tests, resolve the full dependency graph, inspect production runtime flags, or verify container images. Those are deliberate boundaries for the current MVP.

The current value is static migration evidence:

- visible build baseline;
- visible framework baseline;
- visible plugin, wrapper, and container-image baselines;
- explicit risk score and risk reasons;
- build readiness signals;
- backlog-style recommended work items;
- JSON output for automation;
- migration risks that should be reviewed;
- automation suggestions that should be run in a branch;
- a staged plan for engineering review.

## Product Lesson

This case supports the core direction of Modern Java Upgrade Lab: Java modernization reports should be evidence-based and conservative.

For Java 11 and Spring Boot 2.x projects, the useful answer is rarely "use records" or "run one recipe." The useful answer is a staged migration path that identifies framework risk before optional language modernization.

## Next Improvements

This case would become stronger with:

- resolved dependency graph inspection, not only explicitly versioned declarations;
- Gradle dependency-management and BOM inspection;
- Spring Cloud baseline detection;
- CI comparison and dashboard aggregation over generated JSON;
- source repository URL in report metadata;
- optional CI mode with configurable failure thresholds.
