# 011 - English Technical Documentation Baseline

## Date

2026-04-20

## Goal

Align the public repository voice with the intended audience: senior Java engineers, staff engineers, tech leads, and platform teams evaluating Java LTS migrations.

## Decisions

- Use English for public repository content.
- Prefer technical precision over motivational or marketing language.
- Keep the existing engineering-log material traceable while moving the public path to `docs/engineering-log/`.
- Add a dedicated documentation style guide so future contributors have a concrete writing standard.
- Update the current README, contribution guide, index, and latest engineering log entry before broadening the cleanup.
- Treat the remaining Spanish historical notes as explicit documentation debt before a public release announcement.

## Rejected Options

- Leave the historical Spanish path in place. That would keep churn low, but it would weaken the repository now that it is public.
- Translate and rename every historical file in one large commit. That would mix project voice alignment with archival cleanup.
- Keep bilingual public docs. That would weaken the project if the goal is broader open source adoption and conference/blog reuse.

## Rationale

The project is intended to generate community impact beyond a local audience. English-first documentation makes examples, reports, talks, issues, and external references easier to share with the wider Java ecosystem.

The tone also matters. A migration analyzer aimed at experienced engineers should not sound like a feature brochure. It should be explicit about evidence, limitations, trade-offs, and what still requires human review.

## Concrete Result

- The README now frames the project as an evidence-oriented Java migration lab rather than a generic modern-Java feature catalog.
- The contribution guide now defines the audience and links to the documentation style guide.
- The engineering log index now defines the expected language and tone.
- A new documentation style guide captures the writing rules for future work.

## Implementation Notes

The repository now uses `docs/engineering-log/` for public documentation. Historical filenames were renamed to English to make the project easier to browse from GitHub.

## Verification

This is a documentation-only change. I still ran the Maven test suite after the edit to ensure the workspace remains healthy.

## Content Angle

This decision can become a short article section: "Why a Java migration tool should sound like an architecture review, not a feature announcement."

## Next Step

Keep future engineering log entries in English and avoid committing internal planning artifacts as public documentation unless they are intentionally edited for external readers.
