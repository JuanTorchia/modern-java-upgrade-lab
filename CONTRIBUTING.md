# Contributing

Thanks for helping build Modern Java Upgrade Lab.

This project is designed for community impact. Contributions should make Java migration easier to understand, measure, explain, or execute.

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

Be practical and honest. Do not present modern Java features as universal upgrades. Explain when a recommendation is optional or context-dependent.

## Development

The tool is built with Java 25 and Maven.

```bash
mvn test
```

## Bitacora

Project decisions are recorded under `docs/bitacora/`. If a contribution changes project direction, add or update a bitacora entry.
