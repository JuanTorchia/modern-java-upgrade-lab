# Modern Java Upgrade Lab Design

## Purpose

Modern Java Upgrade Lab is an open source tool and educational lab for teams migrating Java applications across LTS generations.

The project should not be a superficial catalog of language features. Its value is to help developers understand migration readiness with evidence from real projects: build configuration, framework versions, dependencies, source patterns, tooling, runtime behavior, and modernization opportunities.

The public positioning is:

> Evidence-based Java LTS migration reports for teams moving from Java 8/11/17/21 to modern Java.

## Product Bet

The strongest version of this project is not "what is new in Java 25". The stronger bet is:

> Do not think about Java as isolated releases. Look at the full evolution across language, concurrency, performance, observability, tooling, and migration paths.

This keeps the project useful beyond Java 25 and gives it a community role: a shared place to learn, compare, measure, and discuss Java modernization.

## Primary Users

### Tech leads and staff engineers

They need evidence to plan and justify Java upgrades. They care about blockers, risks, sequencing, and business-facing explanations.

### Java and Spring Boot teams

They need practical migration guidance from Java 8, 11, 17, or 21 toward newer LTS versions. Their pain is usually not syntax; it is build tooling, framework compatibility, dependencies, CI, runtime behavior, and test confidence.

### Platform teams and consultants

They need repeatable reports across many repositories. They value consistency, readable output, and findings that can be turned into migration tickets.

### Educators, speakers, and content creators

They need reproducible examples, before/after comparisons, and credible narratives for articles and talks.

### Open source maintainers

They need to decide which Java baseline to support and what modern features can be adopted without harming users.

## Community Impact Strategy

The project should be designed as a community artifact, not only as a CLI.

Every meaningful feature should eventually produce:

- a detector or rule;
- a reproducible example;
- a sample report;
- documentation explaining the finding;
- at least one contributor-friendly issue.

The tone should be practical and honest. The project should avoid claiming that migrations are automatic. It should distinguish between:

- blockers that must be fixed;
- risks that need human review;
- opportunities that may be valuable;
- modern Java features that are not worth adopting yet.

## MVP Scope

The first release should focus on one high-impact path:

> Analyze a Maven + Spring Boot application and generate a Markdown migration readiness report for a target Java LTS version.

The initial targets are Java 17, Java 21, and Java 25. The analyzer should support projects currently declaring Java 8, 11, 17, 21, or 25.

### Included

- CLI command: `mjul analyze --path . --target 21`
- Maven project detection.
- Java version detection from Maven properties and compiler plugin configuration.
- Spring Boot version detection.
- Basic dependency and plugin inventory.
- Markdown report generation.
- Finding severities: blocker, risk, opportunity, info.
- Suggested OpenRewrite recipes and commands.
- One Spring Boot Java 8 example project.
- One sample report committed to the repository.
- Project bitacora documenting decisions step by step in first-person style.

### Excluded From MVP

- Dashboard or web UI.
- HTML report.
- Deep Gradle support.
- Automatic OpenRewrite execution.
- Broad JMH benchmark suite.
- Aggressive recommendations for virtual threads, structured concurrency, or scoped values.
- Full source-code modernization engine.

These exclusions are deliberate. They keep the first release credible, testable, and easy for the community to understand.

## Recommended Technical Stack

- Java 25 as the development and runtime JDK for the tool.
- Maven multi-module repository.
- Picocli for the CLI.
- JUnit 5 and AssertJ for tests.
- Maven Model APIs for `pom.xml` inspection.
- JavaParser in a later milestone for source pattern detection.
- OpenRewrite as an integration and recommendation layer, not the first internal engine.

## Architecture

The first architecture should be modular without becoming overengineered.

```text
modern-java-upgrade-lab/
  cli/
    Picocli application and command wiring
  analyzer-core/
    Analysis model, findings, rule engine, report model
  build-inspectors/
    Maven inspector first, Gradle later
  rewrite-adapter/
    OpenRewrite recipe suggestions and command rendering
  examples/
    Spring Boot sample projects
  reports/
    Sample reports generated from examples
  docs/
    Vision, roadmap, blog ideas, talks, and bitacora
```

### Data Flow

```text
Project path
  -> build detection
  -> Java version detection
  -> framework and dependency detection
  -> migration rule evaluation
  -> findings
  -> Markdown report
  -> optional OpenRewrite guidance
```

## Finding Model

Each finding should contain:

- id;
- title;
- severity;
- affected area;
- explanation;
- evidence from the analyzed project;
- recommendation;
- target Java version;
- optional OpenRewrite recipe or command.

Example categories:

- build configuration;
- Spring Boot compatibility;
- dependency compatibility;
- removed or deprecated APIs;
- JVM/runtime flags;
- language modernization opportunities;
- observability and JFR opportunities.

## Report Philosophy

The report should be useful to both engineers and technical leaders.

It should answer:

- What version am I on?
- What target did I ask for?
- What blocks the migration?
- What risks should I review?
- What can be automated?
- What modernization opportunities are worth considering?
- What should I avoid doing for now?

The report must be written in clear language. It should not bury the user in feature lists.

## Bitacora Requirement

The project must keep a step-by-step bitacora under `docs/bitacora/`.

The bitacora is not a dry changelog. It is a first-person source log that can later become blog posts, talks, LinkedIn posts, or conference material.

Each entry should include:

- date;
- objective;
- what I decided;
- what I discarded;
- why;
- what I learned;
- concrete result;
- how I would tell this in a blog post;
- next step.

The voice should make it easy for the project owner to adapt the text as if they had written the journey step by step.

## Roadmap

### Days 1-15

- Create repository structure.
- Write README, vision, contribution guide, and bitacora index.
- Create Maven multi-module skeleton.
- Add Picocli CLI with `analyze`.
- Generate a basic Markdown report.
- Detect Maven and declared Java version.

### Days 16-30

- Detect Spring Boot version.
- Add first finding model.
- Add Java 8 to 17 migration rules.
- Add Spring Boot Java 8 sample app.
- Commit first sample report.

### Days 31-45

- Add Java 17 to 21 rules.
- Suggest OpenRewrite recipes.
- Improve report language.
- Publish first blog outline.

### Days 46-60

- Add Java 21 to 25 rules.
- Add migration path comparison content.
- Add contributor documentation for rules and fixtures.

### Days 61-75

- Add JavaParser-based source pattern detection.
- Detect candidates for records, switch expressions, text blocks, and risky ThreadLocal usage.
- Add a small JFR lab.

### Days 76-90

- Consider HTML report if Markdown is already strong.
- Test against small open source projects.
- Prepare release `0.1.0`.
- Prepare talk/blog kit.

## Initial Repository Content

The initial open source repository should include:

- `README.md`
- `LICENSE`
- `CONTRIBUTING.md`
- `CODE_OF_CONDUCT.md`
- `docs/vision.md`
- `docs/roadmap.md`
- `docs/blog-ideas.md`
- `docs/bitacora/000-index.md`
- `docs/bitacora/001-idea-y-validacion.md`
- Maven modules for CLI and core components.
- GitHub issue templates.

## Initial Issues

- `feat: create Maven multi-module project`
- `feat: add Picocli analyze command`
- `feat: detect Java version from Maven pom.xml`
- `feat: detect Spring Boot version`
- `feat: generate Markdown report`
- `feat: add Java 8 to 17 migration findings`
- `docs: write project vision`
- `docs: add first blog outline`
- `example: add Spring Boot 2 Java 8 sample`
- `good first issue: add detection for maven-compiler-plugin source/target`

## Design Decisions

### Spring Boot first

The first narrative should focus on Spring Boot because it is recognizable, practical, and community-relevant. The analyzer core should remain Java-project oriented, but the first demo should be Spring Boot.

### Markdown first

Markdown reports are easy to review, commit, share, and quote in blog posts. HTML can wait.

### OpenRewrite as a partner, not a replacement

OpenRewrite is valuable for mechanical migration recipes. This project should integrate with it and suggest recipes, but the core identity should be diagnostic and educational.

### No automatic modernization hype

Modern Java features should be recommended only when evidence supports them. Virtual threads, structured concurrency, scoped values, records, sealed classes, switch expressions, and text blocks are useful in context, not as blanket advice.

## Open Questions

- Which open source license should be used? Apache 2.0 is the default recommendation because it is familiar in the Java ecosystem.
- Should the first CLI binary be distributed only through source builds, or should the first release also publish GitHub release artifacts?
- Should the first examples use Spring Boot 2.x only, or include a small Spring Boot 3.x comparison earlier?

## Success Criteria For MVP

The MVP is successful if a Java developer can:

1. clone the repository;
2. run the CLI against the included Spring Boot Java 8 example;
3. read a clear Markdown report;
4. understand the migration risks and next actions;
5. see how to contribute one new detector or rule.

The MVP is not successful if it only lists Java features without helping someone plan a migration.
