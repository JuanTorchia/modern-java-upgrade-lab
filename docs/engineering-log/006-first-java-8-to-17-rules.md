# 006 - First Java 8 to 17 Rules

## Date

2026-04-20

## Goal

Start the second iteration without improvising directly in the CLI. The MVP foundation existed, but it still needed real and extensible rules.

## Decisions

The next step should not be more report text or disconnected modern-Java feature detection. The correct move is a small rule engine and the first Java 8 to Java 17 recommendations.

## Rejected Options

I rejected starting with JavaParser, Gradle, or a dashboard. I also rejected continuing to place findings directly in the CLI because that makes it harder for contributors to understand where migration knowledge lives.

## Rationale

If the project is going to have community impact, rules must be easy to read, test, and discuss. A contributor should be able to open a rule and understand its evidence, risk, and recommendation.

## What I Learned

After the scaffold, there is an important choice: continue building a demo or start building a product. For this project, product means verifiable rules and reports that explain migration decisions.

## Concrete Result

The first cut of iteration 2 was implemented: rule engine in `analyzer-core`, first Java 8 to 17 rules, CLI delegation to the analyzer, and regression coverage so Java 21 OpenRewrite suggestions are not lost.

## Content Angle

After the first MVP, the project could generate a report. That was not enough. If the CLI manufactures findings by hand, the repository is still a demo.

The next question was where migration knowledge should live. The answer was a simple rule engine. Not a plugin system, not a large architecture, just a clear place for evidence, severity, and recommendations.

A useful technical signal appeared during the refactor: moving logic from the CLI to the analyzer almost broke the Java 21 case. That forced a regression test. That kind of early friction is valuable because it shows the project already needs to protect existing behavior, not only add features.

This change marks the real start of the project: from hardcoded report to migration-rule lab.

## Next Step

Review the generated report from a user's perspective and decide whether the next step should improve Markdown sections or add source-code pattern detection.
