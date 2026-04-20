# Contributing

Thanks for helping build Modern Java Upgrade Lab.

This project is designed for evidence-based Java migration work. Contributions should help senior engineers understand, measure, explain, or execute Java LTS migrations with less guesswork.

## Good Contribution Areas

- Migration rules for Java LTS jumps.
- Maven or Gradle detection improvements.
- Spring Boot compatibility findings.
- OpenRewrite recipe documentation.
- Reproducible example projects.
- Clearer report language.
- Blog or talk material derived from real findings.

## Rule Contribution Checklist

See [docs/contributing-rules.md](docs/contributing-rules.md) for the full rule contribution guide.

When adding a rule, include:

- the migration path it applies to;
- the evidence the analyzer should look for;
- why it matters;
- the finding category;
- the severity;
- the recommendation;
- at least one fixture or test.

## Writing Style

Use English for all public repository content. Write for senior Java engineers, staff engineers, tech leads, and platform teams.

Be practical and honest. Do not present modern Java features as universal upgrades. Explain when a recommendation is optional, workload-dependent, framework-dependent, or not yet appropriate for automated migration.

See [docs/documentation-style.md](docs/documentation-style.md) for the repository documentation style guide.

## Development

The tool is built with Java 25 and Maven.

```bash
mvn test
```

## Engineering Log

Project decisions are recorded under `docs/bitacora/`. If a contribution changes project direction, add or update an engineering log entry.
