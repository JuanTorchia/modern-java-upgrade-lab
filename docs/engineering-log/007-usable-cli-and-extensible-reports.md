# 007 - Usable CLI and Extensible Reports

## Date

2026-04-20

## Goal

Improve adoption instead of adding more "intelligence."

The tool could already analyze a Maven/Spring Boot Java 8 example and generate findings, but it still had two practical issues: it was not convenient to execute as an external tool, and the report was growing as a flat list.

For community impact, another engineer must be able to run it quickly and understand the report without reading the code.

## Decisions

I planned an iteration with two goals:

- package the CLI as an executable jar;
- categorize findings so the report can grow into sections.

I also documented how to add a new rule. Contribution should not require understanding the entire project.

## Rejected Options

I rejected a UI at this stage.

I also rejected JavaParser, benchmarks, advanced HTML, and dynamic plugin loading for this iteration. Those may be useful later, but they would add surface area without solving the basic adoption problem: the tool must be easy to try and the report must scale.

## Rationale

An open source tool earns community attention when the first contact is clear.

If someone has to fight the Maven internals to run the CLI, they lose interest. If the report mixes build risks, framework compatibility, and automation suggestions in one flat list, it also becomes weaker as talk or article material.

The minimum flow should be:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21
```

## What I Learned

The project does not need to look bigger to be more serious. It needs better boundaries.

Finding categories are a small detail, but they change the report architecture. Future language, concurrency, performance, observability, and tooling rules can be added without degrading readability.

## Expected Result

By the end of this iteration the project should have:

- an executable jar;
- Markdown reports grouped by section;
- an initial finding taxonomy;
- contributor documentation;
- tests protecting the flow.

## Concrete Result

I implemented `FindingCategory` and classified existing rules as `BASELINE`, `BUILD`, `FRAMEWORK`, and `AUTOMATION`.

Then I changed the Markdown renderer to group findings into sections. The report moved from a flat list to blocks such as `Build & Tooling`, `Framework Compatibility`, `Automation Suggestions`, and `Baseline & Planning`.

I also configured Maven Shade in the `cli` module so the project can generate an executable jar:

```bash
mvn -pl cli -am package
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21
```

The jar smoke test printed a real Markdown report against the Spring Boot Java 8 example.

## Implementation Notes

I first wrote tests that failed because `FindingCategory` and `Finding::category` did not exist. That was the right RED state: the API did not yet support the extension.

Then I added the enum and updated `Finding` constructors. The `analyzer-core` tests passed.

Next, I updated renderer tests to expect `Project Summary` and category sections. They failed because the renderer still used `Summary` and `Findings`. I implemented category grouping with a fixed order.

Finally, I tested packaging. Before Maven Shade, Maven produced `cli-0.1.0-SNAPSHOT.jar`, but not the expected self-contained jar. After configuring the plugin, `modern-java-upgrade-lab-cli.jar` appeared and ran `analyze`.

## Content Angle

"Before adding more rules, I reviewed the project as if I were the first person finding it on GitHub. The conclusion was simple: it did not need a UI. It needed to be easy to run and better at explaining what it already knew."

## Next Step

The next high-impact step is either real source-code pattern detection or Gradle support. The decision depends on which story should come first: deeper migration analysis or broader project coverage.
