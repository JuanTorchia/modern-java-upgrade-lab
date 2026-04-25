# CI Risk Gate

`--fail-on-risk` turns a migration-readiness report into a CI gate. The command still prints or writes the report, then exits with code `2` when the detected risk level is at or above the configured threshold.

Supported thresholds:

```text
LOW
MEDIUM
HIGH
```

## GitHub Actions Example

```yaml
name: Java migration readiness

on:
  pull_request:
  push:
    branches: [main, master]

jobs:
  readiness:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven

      - name: Build CLI
        run: mvn -pl cli -am package

      - name: Analyze migration readiness
        run: |
          java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze \
            --path . \
            --target 21 \
            --format json \
            --fail-on-risk HIGH
```

Use `HIGH` when the gate should block only clear migration risks. Use `MEDIUM` when the repository is actively preparing a migration and the team wants earlier feedback.

