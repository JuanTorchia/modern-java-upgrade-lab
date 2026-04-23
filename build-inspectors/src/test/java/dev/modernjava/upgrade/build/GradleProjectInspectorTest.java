package dev.modernjava.upgrade.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GradleProjectInspectorTest {

    @Test
    void inspectsGroovyGradleProjectMetadataFromFixture() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-groovy-java17-springboot2");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("gradle");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("17");
        assertThat(metadata.springBootVersion()).isEqualTo("2.7.18");
        assertThat(metadata.dependencies()).contains(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-test");
        assertThat(metadata.buildPlugins()).contains(
                "java",
                "org.springframework.boot",
                "io.spring.dependency-management");
    }

    @Test
    void inspectsKotlinGradleProjectMetadataFromFixture() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-java21-springboot3");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("gradle");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.springBootVersion()).isEqualTo("3.3.5");
        assertThat(metadata.dependencies()).contains(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-test");
        assertThat(metadata.buildPlugins()).contains(
                "java",
                "org.springframework.boot",
                "io.spring.dependency-management");
    }

    @Test
    void resolvesKotlinVersionCatalogPluginLibraryAndBundleAliases() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-version-catalog");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("gradle");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.springBootVersion()).isEqualTo("3.3.5");
        assertThat(metadata.dependencies()).containsExactly(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-test",
                "org.assertj:assertj-core");
        assertThat(metadata.buildPlugins()).contains(
                "java",
                "org.springframework.boot",
                "io.spring.dependency-management");
    }

    @Test
    void resolvesGroovyVersionCatalogPluginLibraryAndBundleAliases() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-groovy-version-catalog");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("gradle");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("17");
        assertThat(metadata.springBootVersion()).isEqualTo("2.7.18");
        assertThat(metadata.dependencies()).containsExactly(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-test",
                "org.assertj:assertj-core");
        assertThat(metadata.buildPlugins()).contains(
                "java",
                "org.springframework.boot",
                "io.spring.dependency-management");
    }

    @Test
    void ignoresUnresolvedVersionCatalogAliases() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-version-catalog-unresolved");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.dependencies()).containsExactly("org.springframework.boot:spring-boot-starter-web");
        assertThat(metadata.buildPlugins()).containsExactly("java");
    }

    @Test
    void keepsVisibleBuildFileInspectionWhenVersionCatalogIsMalformed() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-malformed-version-catalog");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.springBootVersion()).isEqualTo("3.3.5");
        assertThat(metadata.dependencies()).containsExactly("org.springframework.boot:spring-boot-starter-web");
        assertThat(metadata.buildPlugins()).contains("java", "org.springframework.boot");
    }

    @Test
    void extractsVisibleGroovyGradleCompilerArgs() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-groovy-java21-preview");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.compilerArgs()).containsExactly("--enable-preview", "-Xlint:preview");
    }

    @Test
    void extractsVisibleKotlinGradleCompilerArgs() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-java21-preview");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.compilerArgs()).containsExactly("--enable-preview");
    }

    @Test
    void ignoresCommentedGradleCompilerArgs() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-commented-preview");

        var metadata = new GradleProjectInspector().inspect(fixturePath);

        assertThat(metadata.compilerArgs()).isEmpty();
    }

    @Test
    void rejectsPathsWithoutAGradleBuildFile() {
        var missingPath = Path.of("src", "test", "resources", "fixtures", "missing-gradle-build");

        assertThatThrownBy(() -> new GradleProjectInspector().inspect(missingPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No Gradle build file found");
    }
}
