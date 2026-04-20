# Modern Java Upgrade Report

Project path: `C:\Users\jstor\develop\modern-java-upgrade\.worktrees\mvp-scaffold\examples\spring-boot-2-java-8`

## Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 21
- Spring Boot version: 2.7.18

## Findings

### [RISK] Spring Boot 2.x needs review before a Java 21 production migration

- Area: Spring Boot 2.x
- Evidence: Detected Spring Boot 2.7.18
- Recommendation: Plan a Spring Boot 3.x migration path and validate framework compatibility before relying on Java 21.
- OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`

### [INFO] OpenRewrite has a Java 21 migration recipe

- Area: Java 21 upgrade
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`
