# Adding Migration Rules

Modern Java Upgrade Lab grows through small, evidence-based migration rules.

A good rule should help a team answer one practical question:

> What did the analyzer see, why does it matter for this target Java version, and what should I do next?

## Rule Shape

Rules implement `MigrationRule` and receive a `RuleContext`:

```java
MigrationRule rule = context -> List.of(new Finding(
        "stable-rule-id",
        FindingCategory.BUILD,
        FindingSeverity.INFO,
        "Build configuration",
        "Short report title",
        "Evidence found in the project",
        "Practical recommendation",
        null));
```

Use `DefaultMigrationRules` for the first catalog. Do not add dynamic plugin loading yet.

## Finding Fields

- `id`: stable lowercase identifier. Include the target version when behavior changes by target.
- `category`: where the finding appears in the report.
- `severity`: importance of the finding.
- `area`: human-readable area shown inside the finding.
- `title`: short sentence for the section heading.
- `evidence`: what the analyzer detected.
- `recommendation`: what the user should do next.
- `openRewriteRecipe`: recipe id when there is a relevant recipe, otherwise `null`.

## Categories

- `BASELINE`: migration planning and source/target Java baseline.
- `BUILD`: Maven, Gradle, compiler plugins, toolchains, CI-related build evidence.
- `FRAMEWORK`: Spring Boot and other framework compatibility.
- `LANGUAGE`: records, pattern matching, switch, text blocks, sealed classes, compact source files.
- `CONCURRENCY`: virtual threads, structured concurrency, scoped values, executor usage.
- `PERFORMANCE`: GC, startup, memory, JIT-sensitive recommendations.
- `OBSERVABILITY`: JFR, logging, metrics, runtime diagnostics.
- `AUTOMATION`: OpenRewrite or other migration automation.
- `RISK`: cross-cutting migration risk that does not fit a narrower category.

## Severity

- `RISK`: could block or materially complicate the migration.
- `INFO`: useful guidance or optional modernization.

Prefer honest `INFO` findings over dramatic warnings. Modern Java features are not mandatory upgrades.

## Testing Expectations

Every rule needs a test that proves:

- when the rule should produce a finding;
- when the rule should stay silent;
- the finding id, category, severity, evidence, and recommendation are stable enough for reports.

Run at least:

```bash
mvn -pl analyzer-core test
```

Before opening a PR, run:

```bash
mvn test
```

## What Not To Do Yet

- Do not run OpenRewrite automatically.
- Do not rewrite user code from the analyzer.
- Do not add a UI for a rule.
- Do not add broad "modernize everything" recommendations.
- Do not suggest language changes without clear evidence.

The project should earn trust by being specific.
