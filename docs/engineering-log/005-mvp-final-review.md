# 005 - MVP Final Review

## Date

2026-04-20

## Goal

Close the first scaffold with a critical review, not just the fact that it compiles.

## Decisions

After the last functional adjustment, I reviewed the architecture and fixed a dependency that was pointing in the wrong direction: `analyzer-core` depended on `rewrite-adapter` only to render an OpenRewrite command suggestion.

## Rejected Options

I rejected leaving that dependency in place for convenience. I also rejected a larger abstraction for migration commands because the project does not yet have enough real complexity to justify it.

## Rationale

The core module should be a stable foundation for analysis and reporting. If it depends on concrete adapters too early, the project becomes harder to extend when Gradle, more rules, or new integrations arrive.

## What I Learned

Even in an MVP, it is worth distinguishing acceptable debt from debt that distorts the project direction. This issue was small, but it affected the central architecture.

## Concrete Result

The report still shows the OpenRewrite recipe and runnable command, but `analyzer-core` no longer depends on `rewrite-adapter`. The scaffold is better aligned with the layered architecture the project should maintain.

## Content Angle

Before calling the first MVP done, I did an uncomfortable review: identify which part of the code was already pushing the design in the wrong direction.

I found a small but revealing dependency. The core module used an OpenRewrite adapter class only to print a command. It worked, but the dependency direction was wrong for a project intended to grow with multiple analyzers and integrations.

I did not do a large refactor. I only removed that coupling and kept the core independent. That is the kind of decision that makes an MVP more serious: not making it bigger, making it coherent.

## Next Step

Run complete verification with JDK 25 and decide whether to merge to the main branch or do one more polish pass on the `analyze` flow.
