# 017 - Public Documentation Audit

## Date

2026-04-23

## Goal

Audit the public documentation surface before promoting the repository to external readers.

The audit focused on README navigation, contribution guidance, issue templates, engineering log entries, sample reports, and local Markdown links.

## Decisions

- Keep the public documentation in English and aimed at experienced Java migration readers.
- Keep the renamed engineering log path as the only public documentation history.
- Normalize verification examples so they do not expose workstation-specific paths.
- Treat local Markdown links as a release-readiness gate.

## Rejected Options

- Do not rewrite historical decisions wholesale. The engineering log should preserve intent and trade-offs, but remove private or machine-specific details.
- Do not add marketing copy to the README. The current positioning is technical and evidence-first, which matches the repository audience.
- Do not introduce a separate documentation checker yet. A small repository can use explicit audit commands until link checking becomes a recurring CI concern.

## Rationale

Public documentation is part of the product surface. Senior engineers evaluating a migration tool will look for coherent navigation, reproducible examples, scoped contribution paths, and evidence that reports are generated from real analyzer behavior.

The repository already had the right documentation structure after the engineering log rename. The remaining risk was small but important: public files should not leak local workstation paths or private planning context, and local links should resolve from a fresh checkout.

## Concrete Result

The audit confirmed that:

- README sample report links resolve locally;
- contribution and documentation style guide links resolve locally;
- engineering log index links resolve locally;
- issue templates are concise and contribution-facing;
- sample reports do not contain local workstation paths;
- the remaining workstation-specific verification command was replaced with a portable Maven command.

## Verification

Local Markdown link audit:

```powershell
python scripts/check-local-markdown-links.py
```

Result: all local Markdown links in README, contribution docs, code of conduct, docs, reports, and issue templates resolved.

Public documentation scan:

Manual ripgrep checks covered workstation-specific paths and private planning references.

Result: no remaining workstation-specific paths or private planning references in public documentation.

## Content Angle

This is a useful public-release lesson: documentation readiness is not only translation or link hygiene. It also means removing private execution context and making the repository understandable from a clean checkout.

## Next Step

Use the remaining research issues to decide the next analyzer capability: Gradle version catalog inspection or an AST-backed source scanning design.
