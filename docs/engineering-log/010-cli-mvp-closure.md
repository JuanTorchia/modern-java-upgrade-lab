# 010 - CLI MVP Closure

Date: 2026-04-20

## Goal

In this step I wanted to start closing the first usable version of Modern Java Upgrade Lab.

At this point, the project did not need more detections. It needed a CLI experience that another engineer could run, evaluate, and debug without me explaining the workflow live.

## Decisions

I focused the MVP closure on CLI usability:

- write reports with `--output`;
- return concise diagnostics without stacktraces for expected user errors;
- add basic CI for contribution validation;
- update the README around real usage rather than project intent only.

## Rejected Options

I rejected adding a UI.

I also rejected installers, Maven Central publication, and new migration rules for this iteration. Those are useful later, but they do not block a serious CLI demo.

## Rationale

An early open source tool needs a credible first-run experience.

If a user runs the CLI and gets a stacktrace because the target directory has no `pom.xml`, `build.gradle`, or `build.gradle.kts`, trust drops immediately. If the same user can write a Markdown report with one command and attach it to a pull request or migration discussion, the project becomes useful.

## Expected Result

The MVP should support a simple demonstration flow:

1. package the CLI;
2. analyze a Maven or Gradle example;
3. write the report to a Markdown file;
4. inspect the generated artifact;
5. explain limitations without hiding them.

## Concrete Result

I added `--output` to the `analyze` command.

The CLI can still print to stdout, but it can now also write the rendered Markdown report to a file. If the destination directory does not exist, the CLI creates it.

I also changed expected error handling. If the target project has no supported Maven or Gradle build file, the CLI returns exit code `1` and prints a short diagnostic. It no longer prints a stacktrace for that case.

Finally, I added GitHub Actions CI to run `mvn test` on pull requests and pushes to `master`.

## Implementation Notes

I first wrote a test for `--output`. The RED state was clear: picocli rejected the option because it did not exist yet.

Then I wrote a test for a directory without a build file. That RED state was useful too: the CLI failed, but it emitted the full stacktrace. That was not acceptable for a first-run experience.

The implementation stayed scoped to `AnalyzeCommand`: report generation did not change, only the output destination and expected error handling changed.

## Verification

I verified the iteration with:

```powershell
mvn test
mvn -pl cli -am package
java -jar cli\target\modern-java-upgrade-lab-cli.jar analyze --path examples\spring-boot-3-gradle-java-21 --target 25 --output target\smoke-reports\gradle-java-25.md
```

I also tested the negative path by pointing the CLI at a directory without a build file. The result was exit code `1` and a concise diagnostic starting with `Error: No Maven or Gradle build file found`.

## Content Angle

"The MVP closure was not about adding more Java modernization checks. It was about making the CLI usable without me standing next to the user explaining every step."

## Next Step

After this closure pass, the project can move into a robustness phase: Gradle version catalogs, multi-module builds, additional source patterns, or JavaParser-backed detection.
