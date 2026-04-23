# 012 - Public Repository Readiness

## Date

2026-04-22

## Goal

Prepare the repository for public discovery after publishing it on GitHub.

The repository was already public, but that is not the same as being ready for external readers. Public readiness requires consistent documentation language, clean navigation, visible CI status, and actionable contributor entry points.

## Decisions

- Rename `docs/bitacora/` to `docs/engineering-log/`.
- Rename historical engineering log files to English filenames.
- Rewrite historical engineering log entries in English with a technical tone for experienced Java engineers.
- Remove internal planning artifacts from `docs/superpowers/` because they were not edited for public readers.
- Add a CI badge to the README.
- Update GitHub Actions from `actions/checkout@v4` and `actions/setup-java@v4` to `v5` to avoid the Node.js 20 deprecation warning shown by GitHub Actions.
- Create initial GitHub issues for contributors after the documentation cleanup is pushed.

## Rejected Options

- Keep Spanish historical notes in the public repository. That would contradict the documentation standard and make the project less accessible to the broader Java ecosystem.
- Keep internal planning notes under `docs/`. Those notes are useful for development history, but they are not contribution-facing documentation.
- Add new analyzer functionality before public readiness. More rules would not fix repository trust, navigation, or contributor onboarding.

## Rationale

For an open source migration tool, credibility starts before the first command is executed. Senior engineers evaluating the project will inspect documentation, CI, issue quality, and scope discipline.

A repository that mixes public docs with raw internal planning notes sends the wrong signal. The project can keep a detailed engineering log, but it should be intentional, readable, and useful to external readers.

## Concrete Result

The public documentation surface is now cleaner:

- engineering logs live under `docs/engineering-log/`;
- historical log content is in English;
- internal planning artifacts are not part of the public docs;
- README shows CI status;
- contributor-facing paths reference the engineering log consistently;
- the CI workflow uses current official GitHub Actions versions.

## Verification

I verified this pass with:

```powershell
cmd /c "set JAVA_HOME=C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10&& set PATH=C:\Users\jstor\.codex\jdks\temurin-25\jdk-25.0.2+10\bin;%PATH%&& mvn test"
```

Result: 30 tests passed. Maven still emits the known JDK 25 `sun.misc.Unsafe` warning from Maven/Guice, but the build succeeds.

The repository should also be checked with GitHub Actions after pushing to `master`.

## Content Angle

This step can become a short "public readiness checklist" section in a build-in-public article:

> Publishing a repository is not the same as making it readable. Before asking for contributors, I cleaned the docs, removed internal notes, made the engineering log intentional, and turned project gaps into issues.

## Next Step

Create the first GitHub issues and labels so contributors have concrete, scoped entry points.
