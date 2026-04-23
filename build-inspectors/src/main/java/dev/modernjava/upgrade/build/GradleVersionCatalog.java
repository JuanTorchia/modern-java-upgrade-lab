package dev.modernjava.upgrade.build;

import dev.modernjava.upgrade.core.InspectorDiagnostic;
import dev.modernjava.upgrade.core.InspectorDiagnosticSeverity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

final class GradleVersionCatalog {

    private static final GradleVersionCatalog EMPTY =
            new GradleVersionCatalog(Map.of(), Map.of(), Map.of(), List.of());
    private static final Path CATALOG_PATH = Path.of("gradle", "libs.versions.toml");
    private static final String DIAGNOSTIC_SOURCE = "Gradle version catalog";

    private final Map<String, CatalogLibrary> libraries;
    private final Map<String, CatalogPlugin> plugins;
    private final Map<String, List<String>> bundles;
    private final List<InspectorDiagnostic> diagnostics;

    private GradleVersionCatalog(
            Map<String, CatalogLibrary> libraries,
            Map<String, CatalogPlugin> plugins,
            Map<String, List<String>> bundles,
            List<InspectorDiagnostic> diagnostics) {
        this.libraries = libraries;
        this.plugins = plugins;
        this.bundles = bundles;
        this.diagnostics = List.copyOf(diagnostics);
    }

    static GradleVersionCatalog read(Path projectRoot) {
        var catalogFile = projectRoot.resolve(CATALOG_PATH);
        if (!Files.isRegularFile(catalogFile)) {
            return EMPTY;
        }

        TomlParseResult result;
        try {
            result = Toml.parse(Files.readString(catalogFile));
        } catch (IOException exception) {
            return withDiagnostic("Could not read version catalog; catalog aliases were skipped.");
        }
        if (result.hasErrors()) {
            return withDiagnostic("Could not parse version catalog; catalog aliases were skipped.");
        }

        var versions = collectVersions(result);
        return new GradleVersionCatalog(
                collectLibraries(result),
                collectPlugins(result, versions),
                collectBundles(result),
                List.of());
    }

    List<InspectorDiagnostic> diagnostics() {
        return diagnostics;
    }

    private static GradleVersionCatalog withDiagnostic(String message) {
        return new GradleVersionCatalog(
                Map.of(),
                Map.of(),
                Map.of(),
                List.of(new InspectorDiagnostic(
                        DIAGNOSTIC_SOURCE,
                        InspectorDiagnosticSeverity.WARNING,
                        message,
                        CATALOG_PATH)));
    }

    void addDependencies(String reference, Set<String> dependencies) {
        if (reference.startsWith("libs.bundles.")) {
            var bundle = bundles.get(reference.substring("libs.bundles.".length()));
            if (bundle == null) {
                return;
            }
            for (String alias : bundle) {
                var library = libraries.get(alias);
                if (library != null) {
                    dependencies.add(library.coordinate());
                }
            }
            return;
        }

        var library = libraries.get(reference.substring("libs.".length()));
        if (library != null) {
            dependencies.add(library.coordinate());
        }
    }

    void addPlugin(String reference, Set<String> buildPlugins) {
        var plugin = plugins.get(reference.substring("libs.plugins.".length()));
        if (plugin != null) {
            buildPlugins.add(plugin.id());
        }
    }

    String springBootVersion(String content) {
        var matcher = GradleProjectInspector.PLUGIN_CATALOG_REFERENCE.matcher(content);
        while (matcher.find()) {
            var plugin = plugins.get(matcher.group(1).substring("libs.plugins.".length()));
            if (plugin != null && "org.springframework.boot".equals(plugin.id())) {
                return plugin.version();
            }
        }
        return null;
    }

    private static Map<String, String> collectVersions(TomlTable catalog) {
        var versionsTable = catalog.getTable("versions");
        if (versionsTable == null) {
            return Map.of();
        }

        var versions = new java.util.LinkedHashMap<String, String>();
        for (var entry : versionsTable.entrySet()) {
            if (entry.getValue() instanceof String version) {
                versions.put(entry.getKey(), version);
                versions.put(normalizeAlias(entry.getKey()), version);
            }
        }
        return Map.copyOf(versions);
    }

    private static Map<String, CatalogLibrary> collectLibraries(TomlTable catalog) {
        var librariesTable = catalog.getTable("libraries");
        if (librariesTable == null) {
            return Map.of();
        }

        var libraries = new java.util.LinkedHashMap<String, CatalogLibrary>();
        for (var entry : librariesTable.entrySet()) {
            var coordinate = libraryCoordinate(entry.getValue());
            if (coordinate != null) {
                libraries.put(normalizeAlias(entry.getKey()), new CatalogLibrary(coordinate));
            }
        }
        return Map.copyOf(libraries);
    }

    private static String libraryCoordinate(Object value) {
        if (value instanceof String coordinate) {
            return groupAndName(coordinate);
        }
        if (!(value instanceof TomlTable table)) {
            return null;
        }

        if (table.isString("module")) {
            return groupAndName(table.getString("module"));
        }

        if (table.isString("group") && table.isString("name")) {
            return table.getString("group") + ":" + table.getString("name");
        }
        return null;
    }

    private static String groupAndName(String coordinate) {
        var parts = coordinate.split(":");
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }
        return parts[0].trim() + ":" + parts[1].trim();
    }

    private static Map<String, CatalogPlugin> collectPlugins(TomlTable catalog, Map<String, String> versions) {
        var pluginsTable = catalog.getTable("plugins");
        if (pluginsTable == null) {
            return Map.of();
        }

        var plugins = new java.util.LinkedHashMap<String, CatalogPlugin>();
        for (var entry : pluginsTable.entrySet()) {
            if (entry.getValue() instanceof TomlTable table && table.isString("id")) {
                plugins.put(normalizeAlias(entry.getKey()), new CatalogPlugin(
                        table.getString("id"),
                        pluginVersion(table, versions)));
            }
        }
        return Map.copyOf(plugins);
    }

    private static String pluginVersion(TomlTable plugin, Map<String, String> versions) {
        if (plugin.isString("version")) {
            return plugin.getString("version");
        }

        if (!plugin.isString("version.ref")) {
            return null;
        }

        var versionRef = plugin.getString("version.ref");
        return versions.getOrDefault(versionRef, versions.get(normalizeAlias(versionRef)));
    }

    private static Map<String, List<String>> collectBundles(TomlTable catalog) {
        var bundlesTable = catalog.getTable("bundles");
        if (bundlesTable == null) {
            return Map.of();
        }

        var bundles = new java.util.LinkedHashMap<String, List<String>>();
        for (var entry : bundlesTable.entrySet()) {
            if (entry.getValue() instanceof TomlArray array) {
                var aliases = array.toList().stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(GradleVersionCatalog::normalizeAlias)
                        .toList();
                bundles.put(normalizeAlias(entry.getKey()), aliases);
            }
        }
        return Map.copyOf(bundles);
    }

    private static String normalizeAlias(String alias) {
        return alias.trim().replace('-', '.').replace('_', '.');
    }

    private record CatalogLibrary(String coordinate) {
    }

    private record CatalogPlugin(String id, String version) {
    }
}
