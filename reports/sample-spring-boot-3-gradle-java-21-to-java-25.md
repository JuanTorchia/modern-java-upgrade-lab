# Modern Java Upgrade Report

Project path: `examples/spring-boot-3-gradle-java-21`

## Project Summary

- Build tool: Gradle
- Declared Java version: 21
- Target Java version: 25
- Spring Boot version: 3.3.5

## Language Modernization

### [INFO] Map-based response can be reviewed as an explicit DTO or record

- Area: Language modernization
- Evidence: src\main\java\dev\modernjava\upgrade\example\GradleGreetingController.java:11 contains `Map<String, Object> hello() {`
- Recommendation: Review whether this loosely typed map represents a stable response shape. If it does, model it as a DTO now and consider a record when the target runtime supports it.

## Automation Suggestions

### [INFO] OpenRewrite has a Java 25 migration recipe

- Area: Migration automation
- Evidence: Target Java version is 25
- Recommendation: Review and run the suggested OpenRewrite recipe in a branch before manual changes.
- OpenRewrite recipe: `org.openrewrite.java.migrate.UpgradeToJava25`
- OpenRewrite command: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25 -Drewrite.exportDatatables=true`
