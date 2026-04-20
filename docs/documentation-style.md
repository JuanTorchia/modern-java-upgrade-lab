# Documentation Style Guide

Modern Java Upgrade Lab documentation is written for senior Java engineers, staff engineers, tech leads, and platform teams evaluating Java LTS migrations.

## Language

Use English for all public repository content:

- README files;
- engineering log entries;
- issue templates;
- contribution guides;
- sample reports;
- roadmap and planning documents.

Avoid mixing Spanish and English in public-facing docs. If a working note starts in another language, translate it before committing.

## Tone

Prefer technical precision over marketing language.

Good:

- "Detects visible Gradle build-file evidence without executing Gradle."
- "Reports migration opportunities that require engineer review before automated transformation."
- "This finding is advisory because the performance impact depends on workload shape."

Avoid:

- "Modernize your Java instantly."
- "Upgrade safely with one command."
- "Use virtual threads everywhere."

## Audience Assumptions

Assume the reader understands:

- Java LTS migration trade-offs;
- Maven and Gradle project structure;
- Spring Boot baseline compatibility;
- CI and dependency-management constraints;
- the difference between detection, recommendation, and automated transformation.

## Evidence First

Every recommendation should answer:

1. What did the tool observe?
2. Why does it matter for the target Java version?
3. What should an engineer review next?
4. What is explicitly out of scope?

## Engineering Log Entries

Use this structure when practical:

- Date;
- Goal;
- Decisions;
- Rejected Options;
- Rationale;
- Concrete Result;
- Implementation Notes;
- Verification;
- Content Angle;
- Next Step.

## Public Release Gate

Before a public announcement or external call for contributors, audit `docs/` for Spanish working notes, stale internal planning artifacts, and mixed-language filenames. Historical notes may remain during active development, but release-facing documentation should be English-only and technically coherent.
