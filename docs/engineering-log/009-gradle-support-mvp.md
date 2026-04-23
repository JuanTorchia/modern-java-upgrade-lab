# 009 - Gradle Support MVP

## Date

2026-04-20

## Goal

Make Modern Java Upgrade Lab useful beyond Maven projects.

Gradle is common enough in Spring Boot projects that delaying it would weaken the project's community usefulness.

## Decisions

I started with minimal, textual, honest Gradle support.

The tool reads `build.gradle` and `build.gradle.kts`, then detects visible Java version declarations, Spring Boot plugin version, dependencies, and plugins.

## Rejected Options

I rejected executing Gradle or resolving the effective Gradle model.

I also rejected deep support for multi-module builds, `buildSrc`, version catalogs, and internal convention plugins. Those are important, but including them now would make the MVP too broad.

## Rationale

For the MVP, it matters more that the tool is fast on common projects and does not do surprising things when analyzing unfamiliar code.

Executing Gradle can trigger plugins, download dependencies, or depend on local environment state. For a first analysis version, conservative file inspection is the better trade-off.

## Expected Result

The CLI should run against a Gradle Spring Boot example and produce a report comparable to the Maven report.

The promise is not "I fully understand Gradle." The promise is "I can detect visible build evidence and provide a first migration report."

## Concrete Result

I added `GradleProjectInspector` to read `build.gradle` and `build.gradle.kts`.

The inspector detects:

- `buildTool = gradle`;
- Java version from `sourceCompatibility`, `targetCompatibility`, `JavaVersion.VERSION_XX`, or `JavaLanguageVersion.of(XX)`;
- Spring Boot version from the `org.springframework.boot` plugin;
- dependencies declared with `group:artifact` notation;
- plugins declared with `id '...'`, `id("...")`, or `java` in Kotlin DSL.

Then I added `ProjectInspector`, a small selector that chooses Maven first when `pom.xml` exists and Gradle when `build.gradle` or `build.gradle.kts` exists.

Finally, I connected the CLI to that selector and created `examples/spring-boot-3-gradle-java-21`, a Spring Boot 3 example using Gradle Kotlin DSL and a Java 21 toolchain.

## Implementation Notes

I first wrote tests for Gradle Groovy and Kotlin DSL fixtures. The first RED state was correct: `GradleProjectInspector` did not exist.

During implementation, the first real failure was the Kotlin DSL `java` plugin. The textual pattern was not multiline-aware and missed a line containing only `java`. I fixed it with `Pattern.MULTILINE`.

Then I wrote the `ProjectInspector` selector test. That test validates three important decisions: Maven has priority when both build files exist, Gradle is detected when Maven is absent, and unknown projects fail with a clear message.

Finally, I wrote a CLI test against the Gradle example. The RED state showed that `AnalyzeCommand` still used `MavenProjectInspector` directly. I switched it to `ProjectInspector`, integrating Gradle without changing the analyzer or renderer.

## Content Angle

"Gradle support started with an uncomfortable but healthy decision: do not execute Gradle. For a migration tool that analyzes unfamiliar projects, the first version should prioritize safety, speed, and explainability over absolute precision."

## Next Step

After this MVP, measure real Gradle projects to identify which patterns are missing and decide whether to support version catalogs, multi-module builds, or an optional Tooling API integration.
