# Java Upgrade Scenario Matrix

This matrix exercises Modern Java Upgrade Lab across the currently relevant LTS upgrade paths using local fixtures and pinned open source reference projects.

It is meant to answer two questions:

- how the analyzer behaves when the same project baseline is evaluated against different target Java versions;
- which upgrade paths are covered well today and which still need better rules.

## Scenario Coverage

| Baseline | Target | Project | Build | Spring Boot | Risk | Score | Work items | Findings | Reports |
| --- | --- | --- | --- | --- | --- | ---: | ---: | ---: | --- |
| Java 8 | Java 11 | Spring Boot 2 local fixture | Maven | 2.7.18 | MEDIUM | 55 | 1 | 4 | [MD](../reports/scenario-matrix/java8-maven-springboot2-to-java11.md) / [JSON](../reports/scenario-matrix/java8-maven-springboot2-to-java11.json) |
| Java 8 | Java 17 | Spring Boot 2 local fixture | Maven | 2.7.18 | MEDIUM | 45 | 2 | 8 | [MD](../reports/scenario-matrix/java8-maven-springboot2-to-java17.md) / [JSON](../reports/scenario-matrix/java8-maven-springboot2-to-java17.json) |
| Java 8 | Java 21 | Spring Boot 2 local fixture | Maven | 2.7.18 | HIGH | 85 | 2 | 8 | [MD](../reports/scenario-matrix/java8-maven-springboot2-to-java21.md) / [JSON](../reports/scenario-matrix/java8-maven-springboot2-to-java21.json) |
| Java 8 | Java 25 | Spring Boot 2 local fixture | Maven | 2.7.18 | HIGH | 85 | 2 | 8 | [MD](../reports/scenario-matrix/java8-maven-springboot2-to-java25.md) / [JSON](../reports/scenario-matrix/java8-maven-springboot2-to-java25.json) |
| Java 11 | Java 17 | RealWorld Spring Boot Java | Gradle | 2.5.2 | MEDIUM | 55 | 2 | 4 | [MD](../reports/scenario-matrix/java11-gradle-realworld-to-java17.md) / [JSON](../reports/scenario-matrix/java11-gradle-realworld-to-java17.json) |
| Java 11 | Java 21 | RealWorld Spring Boot Java | Gradle | 2.5.2 | HIGH | 100 | 5 | 4 | [MD](../reports/scenario-matrix/java11-gradle-realworld-to-java21.md) / [JSON](../reports/scenario-matrix/java11-gradle-realworld-to-java21.json) |
| Java 11 | Java 25 | RealWorld Spring Boot Java | Gradle | 2.5.2 | HIGH | 100 | 5 | 4 | [MD](../reports/scenario-matrix/java11-gradle-realworld-to-java25.md) / [JSON](../reports/scenario-matrix/java11-gradle-realworld-to-java25.json) |
| Java 17 | Java 21 | Spring PetClinic | Maven | 4.0.3 | LOW | 15 | 2 | 5 | [MD](../reports/scenario-matrix/java17-maven-petclinic-to-java21.md) / [JSON](../reports/scenario-matrix/java17-maven-petclinic-to-java21.json) |
| Java 17 | Java 25 | Spring PetClinic | Maven | 4.0.3 | LOW | 15 | 2 | 4 | [MD](../reports/scenario-matrix/java17-maven-petclinic-to-java25.md) / [JSON](../reports/scenario-matrix/java17-maven-petclinic-to-java25.json) |
| Java 21 | Java 25 | Spring Boot 3 Gradle local fixture | Gradle | 3.3.5 | LOW | 15 | 2 | 4 | [MD](../reports/scenario-matrix/java21-gradle-springboot3-to-java25.md) / [JSON](../reports/scenario-matrix/java21-gradle-springboot3-to-java25.json) |

## Current Read

The strongest cases today are Java 8/11 to Java 21/25, Java 8 to Java 11 with visible legacy signals, and Java 11 Gradle projects with runtime, plugin, and dependency baselines. These produce clear risk, build readiness, migration blockers, suggested commands, and backlog-style work items.

Java 17 to Java 21/25 also works, but the reports are currently more conservative because the sample baselines are already modern. That is acceptable: not every upgrade should look risky.

The Java 8 to Java 11 case is no longer empty. It now reports removed Java EE/JAXB usage, reflective access, and old Surefire/Failsafe baselines when those signals are visible. That moves the report closer to a real migration readiness assessment rather than a generic version check.

The generated portfolio summary at [reports/portfolio-summary.md](../reports/portfolio-summary.md) aggregates all JSON reports and shows the current risk distribution, top blockers, and informational signals across the scenario matrix.

## Coverage Gaps

- Add Java 11 to 17 rules beyond generic baseline scoring.
- Add dependency graph and BOM-managed version resolution.
- Validate more pinned open source projects and record which findings were useful, noisy, or missing.

## Regeneration Commands

The scenario reports are generated with:

```powershell
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 11 --output reports/scenario-matrix/java8-maven-springboot2-to-java11.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 17 --output reports/scenario-matrix/java8-maven-springboot2-to-java17.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21 --output reports/scenario-matrix/java8-maven-springboot2-to-java21.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 25 --output reports/scenario-matrix/java8-maven-springboot2-to-java25.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/realworld-springboot-java --target 17 --output reports/scenario-matrix/java11-gradle-realworld-to-java17.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/realworld-springboot-java --target 21 --output reports/scenario-matrix/java11-gradle-realworld-to-java21.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/realworld-springboot-java --target 25 --output reports/scenario-matrix/java11-gradle-realworld-to-java25.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/spring-petclinic --target 21 --output reports/scenario-matrix/java17-maven-petclinic-to-java21.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path .worktrees/reference-cases/spring-petclinic --target 25 --output reports/scenario-matrix/java17-maven-petclinic-to-java25.md
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-3-gradle-java-21 --target 25 --output reports/scenario-matrix/java21-gradle-springboot3-to-java25.md
```
