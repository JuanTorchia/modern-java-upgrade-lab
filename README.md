# Modern Java Upgrade Lab

Evidence-based Java LTS migration reports for teams moving from Java 8/11/17/21 to modern Java.

Modern Java Upgrade Lab is an open source CLI and educational lab for understanding Java modernization across LTS releases. It is not just a list of new language features. It analyzes real projects, identifies migration risks, suggests practical next steps, and creates reproducible material for teams, blogs, and talks.

## MVP Focus

The first MVP focuses on Maven and Gradle Spring Boot projects and generates Markdown reports for target Java LTS versions.

```bash
mjul analyze --path . --target 21
```

During MVP development the CLI can be packaged and executed as a runnable jar:

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

If the project does not contain a supported Maven or Gradle build file, the CLI exits with code `1` and prints a short error message instead of a stacktrace.

The report is meant to answer:

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

This repository is in early MVP development. The CLI can analyze the included Maven and Gradle Spring Boot examples, print readable Markdown migration reports, or write them to files with `--output`.

CI runs `mvn test` on pull requests and pushes to `master`.

## Project Principles

- Be useful before being flashy.
- Prefer honest diagnostics over automatic magic.
- Integrate with OpenRewrite where it helps.
- Keep examples reproducible.
- Document decisions in the bitacora.
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
