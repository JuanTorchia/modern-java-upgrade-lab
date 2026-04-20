# Modern Java Upgrade Lab

Evidence-based Java LTS migration reports for teams moving from Java 8/11/17/21 to modern Java.

Modern Java Upgrade Lab is an open source CLI and engineering lab for evidence-based Java modernization across LTS releases. It is not a catalog of language features. It inspects real projects, reports migration risks, identifies modernization candidates, and produces artifacts that senior engineers can review in architecture discussions, migration planning sessions, and technical write-ups.

## MVP Focus

The first MVP focuses on Maven and Gradle Spring Boot projects and generates Markdown reports for target Java LTS versions.

```bash
mjul analyze --path . --target 21
```

During MVP development, package and execute the CLI as a runnable jar:

```bash
mvn -pl cli -am package
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-3-gradle-java-21 --target 25
```

To save a report to a Markdown file:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze \
  --path examples/spring-boot-3-gradle-java-21 \
  --target 25 \
  --output reports/local-gradle-java-21-to-25.md
```

If the project does not contain a supported Maven or Gradle build file, the CLI exits with code `1` and prints a short diagnostic instead of a stacktrace.

The report is designed to answer:

- which Java version the project declares;
- which build tool and framework baseline were detected;
- which migration risks are visible from static project evidence;
- which OpenRewrite recipes may be relevant;
- which source-level modernization candidates are worth reviewing;
- which changes should stay out of scope until the migration baseline is stable.

## Why This Exists

Java migrations are rarely syntax migrations. Real teams deal with build plugins, framework compatibility, dependency baselines, CI behavior, runtime flags, observability, and test confidence.

This project treats Java modernization as a staged, evidence-based migration path across LTS generations.

## Current Status

This repository is in early MVP development. The CLI can analyze the included Maven and Gradle Spring Boot examples, print Markdown migration reports, or write them to files with `--output`.

CI runs `mvn test` on pull requests and pushes to `master`.

## Project Principles

- Be useful before being flashy.
- Prefer precise diagnostics over automated magic.
- Integrate with OpenRewrite where it helps.
- Keep examples reproducible.
- Document decisions in the engineering log.
- Make contribution paths small and clear.

## MVP Repository Areas

During MVP development, the repository is organized around these current and planned areas:

- `cli/` - command-line application.
- `analyzer-core/` - analysis model, findings, and report rendering.
- `build-inspectors/` - Maven and Gradle project inspection.
- `rewrite-adapter/` - OpenRewrite recipe suggestions.
- `examples/` - reproducible migration examples.
- `reports/` - sample generated reports.
- `docs/` - vision, roadmap, blog ideas, talks, and bitacora.

## Contributing

Good first contributions include:

- adding a migration rule;
- improving a report explanation;
- adding a fixture project;
- adding a Gradle build fixture that documents a common real-world pattern;
- documenting an OpenRewrite recipe;
- testing the analyzer against a real open source project.

See [CONTRIBUTING.md](CONTRIBUTING.md).
