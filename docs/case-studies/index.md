# Open Source Case Studies

These case studies are generated from pinned open source repositories. They are intended to validate Modern Java Upgrade Lab against real project layouts and to provide reviewable examples for Java migration discussions.

## Generated Reports

Narrative case studies:

- [RealWorld Spring Boot Java 11 to Java 21](realworld-springboot-java-11-to-21.md)

Generated reference reports:

| Project | Source commit | Target | Report | Public-readiness |
| --- | --- | --- | --- | --- |
| Spring PetClinic | `c7ee170434ec3e369fdc9201290ba2ea4c92b557` | Java 25 | [reference-spring-petclinic-to-java-25.md](../../reports/reference-spring-petclinic-to-java-25.md) | Good first public demo |
| JHipster Sample App | `1b0730f2d7ad13bb1c71396692b86bb9205e8d7c` | Java 25 | [reference-jhipster-sample-app-to-java-25.md](../../reports/reference-jhipster-sample-app-to-java-25.md) | Good validation case |
| RealWorld Spring Boot Java | `56be3ced4f3134424ead5fcaf387b3aa640b9532` | Java 21 | [reference-realworld-springboot-java-to-java-21.md](../../reports/reference-realworld-springboot-java-to-java-21.md) | Strong migration signal |
| Gothinkster Spring Boot RealWorld | `ee17e31aafe733d98c4853c8b9a74d7f2f6c924a` | Java 21 | [reference-gothinkster-spring-boot-realworld-to-java-21.md](../../reports/reference-gothinkster-spring-boot-realworld-to-java-21.md) | Strong source scanning case |
| Okta Spring Boot + Cloud Example | `d971f6615125e85d0ffddee6574630d8173397ca` | Java 21 | [reference-oktadev-spring-boot-cloud-to-java-21.md](../../reports/reference-oktadev-spring-boot-cloud-to-java-21.md) | Strong multi-module Maven case |

## Current Read

Spring PetClinic and JHipster are useful as recognizable Java 17/21 to Java 25 examples. They prove the report format on current Spring Boot applications, but they are not legacy Java 8 migration examples.

RealWorld Spring Boot Java and Gothinkster Spring Boot RealWorld are better migration demos today because they expose Java 11, Spring Boot 2.x, and Java 21 upgrade planning signals.

The Okta Spring Boot + Cloud example is useful as a multi-module Maven case. The analyzer reads the aggregator POM and consolidates Java and Spring Boot baselines from declared child modules.

The RealWorld report is also available as JSON, which makes it suitable for CI checks, dashboards, and future portfolio-level comparisons.

## Next Improvements Found

- Resolve dependency graphs and BOM-managed versions, not only explicitly versioned declarations.
- Add portfolio aggregation over generated JSON.
- Add source repository URL to analysis metadata when it can be detected.
