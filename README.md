# Modern Java Upgrade Lab

Evidence-based Java LTS migration reports for teams moving from Java 8/11/17/21 to modern Java.

Modern Java Upgrade Lab is an open source CLI and educational lab for understanding Java modernization across LTS releases. It is not just a list of new language features. It analyzes real projects, identifies migration risks, suggests practical next steps, and creates reproducible material for teams, blogs, and talks.

## MVP Focus

The first MVP focuses on Maven + Spring Boot projects and generates Markdown reports for target Java LTS versions.

```bash
mjul analyze --path . --target 21
```

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
