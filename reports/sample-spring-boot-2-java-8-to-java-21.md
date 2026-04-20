# Modern Java Upgrade Report

Project path: `examples/spring-boot-2-java-8`

## Summary

- Build tool: Maven
- Declared Java version: 8
- Target Java version: 21
- Spring Boot version: 2.7.18

## Findings

### [RISK] Spring Boot 2.x finding

- Area: Spring Boot 2.x
- Evidence: Spring Boot 2.7.18
- Recommendation: Spring Boot 2.x needs review before a Java 21 production migration
- OpenRewrite recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`

### [INFO] OpenRewrite Java 21 finding

- Area: Java 21 upgrade
- Evidence: Target Java version is 21
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava21`

## Recipes

- `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2`
- `org.openrewrite.java.migrate.UpgradeToJava21`
