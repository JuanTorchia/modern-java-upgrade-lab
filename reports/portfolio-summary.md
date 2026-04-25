# Migration Portfolio Summary

## Executive Summary

- Reports analyzed: 10
- Risk levels: MEDIUM=3, HIGH=4, LOW=3
- Target Java versions: Java 17=2, Java 21=3, Java 25=4, Java 11=1
- Build tools: gradle=4, maven=6
- Spring Boot major: Spring Boot 2=7, Spring Boot 4=2, Spring Boot 3=1

## Top Blockers

- FRAMEWORK_BASELINE: 6
- BUILD_PLUGIN: 4
- JAVA_EE_REMOVED: 4
- REFLECTIVE_ACCESS: 4
- DEPENDENCY_COMPATIBILITY: 3
- RUNTIME_IMAGE: 3

## Top Signals

- AUTOMATION: 9
- BASELINE: 5
- BUILD_PLUGIN: 5
- LANGUAGE_MODERNIZATION: 2
- CONCURRENCY: 1
- FRAMEWORK_BASELINE: 1

## Reports

| Report | Risk | Score | Target | Build | Spring Boot |
| --- | --- | ---: | ---: | --- | --- |
| java11-gradle-realworld-to-java17.json | MEDIUM | 55 | 17 | gradle | 2.5.2 |
| java11-gradle-realworld-to-java21.json | HIGH | 100 | 21 | gradle | 2.5.2 |
| java11-gradle-realworld-to-java25.json | HIGH | 100 | 25 | gradle | 2.5.2 |
| java17-maven-petclinic-to-java21.json | LOW | 15 | 21 | maven | 4.0.3 |
| java17-maven-petclinic-to-java25.json | LOW | 15 | 25 | maven | 4.0.3 |
| java21-gradle-springboot3-to-java25.json | LOW | 15 | 25 | gradle | 3.3.5 |
| java8-maven-springboot2-to-java11.json | MEDIUM | 55 | 11 | maven | 2.7.18 |
| java8-maven-springboot2-to-java17.json | MEDIUM | 45 | 17 | maven | 2.7.18 |
| java8-maven-springboot2-to-java21.json | HIGH | 85 | 21 | maven | 2.7.18 |
| java8-maven-springboot2-to-java25.json | HIGH | 85 | 25 | maven | 2.7.18 |