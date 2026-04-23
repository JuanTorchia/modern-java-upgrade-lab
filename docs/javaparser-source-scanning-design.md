# JavaParser-Backed Source Scanning Design

## Goal

Define the next source analysis layer for Modern Java Upgrade Lab without turning the analyzer into a compiler, rewrite engine, or plugin platform.

The current `SourcePatternScanner` is intentionally textual. It is fast, easy to inspect, and good enough for narrow evidence such as direct `ThreadLocal` or `sun.misc.Unsafe` usage. Some future rules need AST precision to avoid broad string matching and to map evidence to specific Java constructs.

## Parser Choice

Use JavaParser for the first AST-backed scanner.

Primary JavaParser references describe the library as a Java parser that produces an Abstract Syntax Tree and supports traversal for code analysis. The project also publishes `javaparser-core` for AST parsing and `javaparser-symbol-solver-core` when type and declaration resolution are needed.

References:

- [JavaParser home](https://javaparser.org/)
- [Inspecting an AST](https://javaparser.org/inspecting-an-ast/)
- [JavaParser repository](https://github.com/javaparser/javaparser)
- [JavaParser parse errors and semantic validation](https://javaparser.org/code-style-architecture/)

## Recommended Architecture

Keep a single source scanning entry point:

```text
SourcePatternScanner
  -> textual detectors
  -> AST detectors
  -> List<SourcePattern>
```

Do not add a rule plugin system. Add fixed detector methods for the first AST-backed candidates and keep them close to the existing `SourcePatternScanner` behavior.

Implementation direction:

1. Keep walking Java files once, with the existing ignored-directory behavior.
2. Run existing textual detectors first because they are cheap and already covered.
3. Parse each Java file with JavaParser only for AST detectors.
4. Convert AST matches into the existing `SourcePattern` model:
   - `type`: new `SourcePatternType`;
   - `relativePath`: same project-relative path;
   - `lineNumber`: first line from the AST node range;
   - `evidence`: normalized source line or a short AST-derived summary.
5. Let `DefaultMigrationRules` map the new `SourcePatternType` to a `Finding`, as it does today.

This keeps report rendering, finding IDs, and migration rule evaluation stable.

## First AST-Backed Rule Candidate

Implement `STRUCTURED_CONCURRENCY_PREVIEW` first.

Why this candidate:

- The Java 21 -> 25 rule catalog already lists `structured-concurrency-preview-boundary` as a candidate.
- The finding is advisory and risk-oriented, which matches the analyzer's evidence-first posture.
- The evidence is source-level and benefits from AST precision:
  - imports of structured concurrency preview APIs;
  - qualified type references;
  - object creation or method calls on the structured concurrency API surface.
- The rule should not infer runtime behavior or rewrite code.

Initial report behavior:

- Category: `CONCURRENCY`.
- Severity: `RISK`.
- Target scope: Java 21 -> Java 25 migrations.
- Evidence: file, line, and source construct mentioning structured concurrency preview APIs.
- Recommendation: keep structured concurrency behind explicit preview boundaries and review source/bytecode compatibility on every JDK update.

## Why Not Start With Symbol Solving

Do not add JavaParser symbol solving for the first rule.

The first candidate can start from imports and explicit type usages. Symbol solving adds classpath setup, dependency model questions, generated-code behavior, and more failure modes. It becomes useful later for rules that need to distinguish same-named types or reason across files, but it is not necessary for the first AST layer.

## Performance Expectations

The AST scanner should remain bounded:

- skip ignored directories such as `target`;
- parse only `.java` files under the inspected project path;
- process files deterministically by sorted relative path;
- avoid symbol solving in the first implementation;
- keep detector logic linear in the parsed AST size;
- consider a file-size guard before parsing very large generated files.

For typical service repositories, parsing source once per analysis should be acceptable. If it becomes measurable, add timing metrics or a scanner-level option later.

## Failure Modes

AST scanning must not make the analyzer brittle:

- Parse failures should skip the affected file and preserve textual scan results.
- Unsupported preview syntax should not fail the full analysis.
- Missing classpath information means the first AST layer should avoid type-resolution claims.
- Generated or partially checked-in source may produce false negatives.
- Lombok, annotation processors, and generated sources remain out of scope because the analyzer does not compile or execute the project.

The metadata model currently has no place for scanner diagnostics. Do not add noisy user-facing warnings until there is a stable diagnostic shape.

## Out Of Scope

- Automatic code modification.
- OpenRewrite execution.
- Full semantic analysis or type resolution in the first AST pass.
- Build-system classpath construction.
- A source-rule plugin API.
- Cross-file dataflow or call graph analysis.
- Claims about effective runtime behavior.

## Test Plan For Implementation

When implementation starts, add focused tests before production code:

- A fixture with an import of structured concurrency preview APIs produces one `STRUCTURED_CONCURRENCY_PREVIEW` pattern.
- A fixture with a qualified type reference produces one pattern.
- Comments, string literals, and unrelated same-name identifiers do not produce patterns.
- A malformed Java file does not fail the full scan.
- The migration rule maps the pattern to a `CONCURRENCY` / `RISK` finding only for Java 21 -> 25.
- Existing textual scanner tests continue to pass unchanged.

## Later Candidates

After the first AST rule is stable, consider:

- `virtual-thread-synchronization-reprofile`: detect synchronized methods or blocks inside code paths that create virtual threads.
- `module-import-declarations-review`: identify source files with broad import sets where Java 25 module imports might improve readability.
- `crypto-kdf-api-review`: only after the project can handle more precise API evidence and security review boundaries.
