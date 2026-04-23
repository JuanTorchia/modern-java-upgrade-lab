# 008 - Source Pattern Detection

## Date

2026-04-20

## Goal

Make Modern Java Upgrade Lab inspect real source code, not only Maven or Spring Boot metadata.

The goal is to detect small but useful signals in `.java` files and convert them into explainable modernization opportunities.

## Decisions

I started with a lightweight pattern scanner before introducing JavaParser.

The first patterns are:

- `Map<String, Object>` as a possible DTO candidate for `record`;
- `SimpleDateFormat` as a signal to review `java.time`;
- `Executors.newFixedThreadPool` or `newCachedThreadPool` as a signal to evaluate virtual threads on Java 21+.

## Rejected Options

I rejected automatic code transformation.

I also rejected starting with an AST parser. Not because it is a bad idea, but because the report and architecture first need to prove they can carry code evidence simply.

## Rationale

A migration recommendation is more useful when it points to concrete evidence.

"Java has records" is superficial. "This controller returns `Map<String, Object>` and may be clearer with an explicit DTO" is more useful.

## Expected Result

The report should show a `Language Modernization` section with concrete opportunities from the Spring Boot Java 8 example.

If classic concurrency appears, the report should also show it under `Concurrency`, especially when the target is Java 21 or higher.

## Concrete Result

I added `SourcePattern`, `SourcePatternType`, and `SourcePatternScanner`.

The scanner walks `.java` files, ignores `target/`, ignores imports to reduce noise, and detects three initial patterns:

- `Map<String, Object>`;
- `SimpleDateFormat`;
- `Executors.newFixedThreadPool` and `Executors.newCachedThreadPool`.

Then I extended `ProjectMetadata` to carry those patterns and added rules that convert them into findings:

- `Map<String, Object>` appears as `LANGUAGE`;
- `SimpleDateFormat` appears as `LANGUAGE`;
- `Executors` factories appear as `CONCURRENCY` only for Java 21 or higher.

Finally, I connected the scanner to the CLI. The Spring Boot Java 8 example now generates a `Language Modernization` section because it finds a controller building a response with `Map<String, Object>`.

## Implementation Notes

I first wrote a test that failed because `SourcePatternScanner`, `SourcePattern`, and `SourcePatternType` did not exist.

The first scanner implementation also detected `SimpleDateFormat` in an import. That failure was useful: it forced the scanner to separate actionable evidence from noise. I adjusted it to ignore imports.

Then I wrote an analyzer test to verify that patterns become findings. The RED state showed that `ProjectMetadata` could not carry patterns yet. I added that field and created specific rules.

Finally, I wrote a CLI test expecting `Language Modernization`. The first fixture had no source code, so I switched to the real Spring Boot Java 8 example and connected the scanner in `AnalyzeCommand`.

The jar smoke test exposed another issue: the same file generated two `Map<String, Object>` findings, one for the method signature and one for a local variable. Technically correct, but noisy as a report. I adjusted the scanner to deduplicate by pattern type within each file and keep the first concrete evidence.

The project rule from this iteration is clear: detecting more does not always mean helping more. A migration tool needs actionable signal and low noise.

## Content Angle

"The first value jump was to stop talking about features in the abstract. Instead of saying Java has records, I started looking for code where a record might improve the model. The tool does not change code; it shows evidence for a better migration discussion."

## Next Step

If these patterns prove useful, the next natural step is choosing between:

- adding more simple patterns;
- introducing JavaParser for more precise structure detection.

My current inclination is to add a few more simple patterns before JavaParser, but only when each pattern can be explained with clear report evidence.
