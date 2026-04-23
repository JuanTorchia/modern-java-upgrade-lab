# 002 - MVP Design

## Date

2026-04-20

## Goal

Convert the idea into a concrete MVP that is useful, publishable, and easy for the Java community to understand.

## Decisions

The first MVP should be a CLI that analyzes Maven and Spring Boot projects and generates a Markdown migration-readiness report for a target Java LTS version.

The first strong use case is:

> I have an older Maven + Spring Boot application. I want to know how ready it is for Java 17, 21, or 25, what blocks the migration, what OpenRewrite can automate, and which modernization opportunities are worth reviewing.

The proposed command is:

```bash
mjul analyze --path . --target 21
```

## Rejected Options

I rejected a dashboard, HTML reports, and deep Gradle support for the first cut.

I also rejected executing OpenRewrite automatically from the MVP. Suggesting recipes and commands is more honest and less invasive at this stage.

## Rationale

For community impact, the project must be easy to try and easy to explain.

Markdown is enough for the first report because it works in GitHub, can be versioned, can be copied into an article, and can be reviewed in pull requests.

Spring Boot is a good first story because it represents many real Java projects. The analyzer core should remain generic, but the first public narrative needs a recognizable application type.

## What I Learned

A serious MVP is not the one with the most features. It is the one that answers a real question end to end.

For this project, that question is:

> What should I know before migrating this Java application to a newer LTS release?

## Concrete Result

The MVP includes:

- Picocli-based CLI;
- Java 25 as the tool runtime;
- Maven multi-module build;
- Maven inspection;
- Java version detection;
- Spring Boot version detection;
- finding model;
- Markdown reporting;
- OpenRewrite suggestions;
- Spring Boot Java 8 example;
- project engineering log.

The MVP does not include:

- dashboard;
- HTML reports;
- large benchmarks;
- automatic migration;
- aggressive modern-feature recommendations.

## Content Angle

The temptation when building a modern Java tool is to start with features: records, virtual threads, pattern matching, scoped values.

For a real migration, that is rarely the first conversation. Teams first need to understand declared Java versions, framework baselines, dependency constraints, applicable recipes, and risky changes.

That is why the MVP starts with a report. It does not try to be magical. It tries to be clear.

## Next Step

Create the initial open source repository structure and prepare a task-by-task implementation plan.
