# Gradle Version Catalog Inspection

## Goal

Define how Modern Java Upgrade Lab should inspect Gradle version catalogs without executing Gradle.

The current Gradle inspector reads visible `build.gradle` and `build.gradle.kts` files. That works for direct dependency and plugin declarations, but many Gradle builds keep dependency coordinates and plugin versions in `gradle/libs.versions.toml`.

## Gradle Model To Support

Primary Gradle documentation describes version catalogs as static TOML files that expose type-safe accessors in build scripts:

- `gradle/libs.versions.toml` is the conventional catalog file Gradle imports automatically.
- Catalog TOML has four top-level sections: `[versions]`, `[libraries]`, `[bundles]`, and `[plugins]`.
- Library aliases are referenced through `libs.<alias>`.
- Plugin aliases are referenced through `libs.plugins.<alias>`.
- Alias names can use dash, underscore, or dot separators; generated accessors normalize those separators into dot notation.
- Bundles group library aliases and are referenced through `libs.bundles.<alias>`.
- Catalog versions declare requested versions, but they do not prove the effective dependency version after Gradle conflict resolution.

References:

- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/version_catalogs.html)
- [Gradle dependency management basics: version catalogs](https://docs.gradle.org/current/userguide/dependency_management_basics.html#sec:using-version-catalog)
- [Gradle VersionCatalog API](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api.artifacts/-version-catalog/index.html)
- [Tomlj Java API](https://tomlj.org/docs/java/latest/org/tomlj/package-summary.html)

## Recommended MVP

Add read-only catalog support inside `build-inspectors` without running Gradle:

1. Look for `gradle/libs.versions.toml` relative to the inspected project root.
2. Parse TOML with a real TOML parser rather than regex. `org.tomlj:tomlj` is a reasonable first candidate because it parses TOML and exposes parse errors as structured data.
3. Build a small `VersionCatalogSnapshot` model:
   - `versions`: alias to version constraint text;
   - `libraries`: alias to `group:name` plus optional requested version evidence;
   - `plugins`: alias to plugin id plus optional requested version evidence;
   - `bundles`: alias to ordered library aliases.
4. Normalize catalog aliases to Gradle accessor paths by treating dash, underscore, and dot as accessor separators.
   - `spring-boot-starter-web` maps to `libs.spring.boot.starter.web`.
   - `spring.boot.starter.web` maps to the same accessor path and should be treated as an alias collision if both appear.
5. Extend `GradleProjectInspector` to resolve visible build-file references:
   - Kotlin DSL dependencies such as `implementation(libs.spring.boot.starter.web)`;
   - Groovy DSL dependencies such as `implementation libs.spring.boot.starter.web`;
   - plugin aliases such as `alias(libs.plugins.spring.boot)`;
   - bundle references such as `testImplementation(libs.bundles.testing)` or `testImplementation libs.bundles.testing`.
6. Preserve the existing metadata shape:
   - resolved libraries add `group:name` entries to `ProjectMetadata.dependencies()`;
   - resolved plugin aliases add plugin ids to `ProjectMetadata.buildPlugins()`;
   - a resolved Spring Boot plugin alias can populate `ProjectMetadata.springBootVersion()` when the plugin id is `org.springframework.boot` and the catalog exposes a requested version.

This keeps catalog support as another static evidence source. The analyzer should describe what the catalog requests, not what Gradle ultimately resolves.

## Out Of Scope For MVP

- Executing Gradle, importing the Gradle Tooling API, or resolving configurations.
- Evaluating `settings.gradle(.kts)` catalog declarations beyond the conventional `gradle/libs.versions.toml` file.
- Multiple named catalogs such as `testLibs` or catalogs imported from custom files.
- `buildSrc`, included builds, precompiled convention plugins, or binary convention plugins.
- Dynamic alias access such as `libs.findLibrary("...")`.
- Variables, string interpolation, conditional build logic, or plugin-management logic.
- Dependency conflict resolution, platforms, enforced platforms, constraints, or effective version calculation.
- Treating malformed TOML as a hard project inspection failure. The inspector should keep visible build-file evidence and add a reportable diagnostic later when the metadata model has a place for inspector warnings.

## Test Plan For Implementation

If code is added after this spike, include fixtures for both Gradle DSLs:

- Kotlin DSL fixture with `plugins { alias(libs.plugins.spring.boot) }` and dependencies using `implementation(libs.spring.boot.starter.web)`.
- Groovy DSL fixture with `plugins { alias(libs.plugins.spring.boot) }` and dependencies using `implementation libs.spring.boot.starter.web`.
- Bundle fixture where `testImplementation(libs.bundles.testing)` expands only aliases that resolve to catalog libraries.
- Alias normalization fixture proving `spring-boot-starter-web` maps to `libs.spring.boot.starter.web`.
- Negative fixture proving unresolved catalog aliases do not create guessed dependencies.
- Malformed TOML fixture proving the visible build-file inspection still works.

## Implementation Sequence

1. Add catalog parsing and snapshot tests independent of `GradleProjectInspector`.
2. Add Kotlin DSL alias resolution in `GradleProjectInspector`.
3. Add Groovy DSL alias resolution.
4. Add plugin alias resolution for `buildPlugins()` and Spring Boot version detection.
5. Add bundle expansion.
6. Add report-facing diagnostics only after the analyzer metadata model supports inspector warnings.
