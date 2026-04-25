# Getting Started

Modern Java Upgrade Lab is a local CLI. It analyzes a Maven or Gradle project and writes migration-readiness reports for Java LTS upgrades.

## Requirements

- JDK 21 or newer for building this repository.
- Maven 3.9 or newer.
- A Maven or Gradle project to analyze.

The analyzer reads project files. It does not require a database, server, cloud account, or repository credentials.

## Build The CLI

From the repository root:

```bash
mvn -pl cli -am package
```

The runnable jar is written to:

```text
cli/target/modern-java-upgrade-lab-cli.jar
```

## Analyze A Project

Print a Markdown report:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path . --target 21
```

Write a Markdown report:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze \
  --path . \
  --target 21 \
  --output reports/local-java-21-readiness.md
```

Write JSON for automation or portfolio aggregation:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze \
  --path . \
  --target 21 \
  --format json \
  --output reports/local-java-21-readiness.json
```

## Portfolio Summary

After generating JSON reports for multiple repositories, aggregate them locally:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar portfolio \
  --input reports/scenario-matrix \
  --output reports/portfolio-summary.md
```

The portfolio report groups risk levels, target Java versions, build tools, Spring Boot majors, migration blockers, and informational signals.

## Exit Codes

| Code | Meaning |
| ---: | --- |
| 0 | The command completed successfully. |
| 1 | The command could not analyze the input or read/write the requested files. |
| 2 | The project was analyzed successfully, but `--fail-on-risk` matched or exceeded the configured threshold. |

