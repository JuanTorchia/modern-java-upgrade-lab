package dev.modernjava.upgrade.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MavenProjectInspectorTest {

    @Test
    void inspectsMavenProjectMetadataFromFixture() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "maven-java8-springboot2");

        var metadata = new MavenProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("maven");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("8");
        assertThat(metadata.springBootVersion()).isEqualTo("2.7.18");
        assertThat(metadata.dependencies()).contains("org.springframework.boot:spring-boot-starter-web");
        assertThat(metadata.buildPlugins()).contains("org.apache.maven.plugins:maven-compiler-plugin");
    }

    @Test
    void extractsVisibleCompilerArgsFromMavenCompilerPlugin() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "maven-java21-preview");

        var metadata = new MavenProjectInspector().inspect(fixturePath);

        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
        assertThat(metadata.compilerArgs()).containsExactly("--enable-preview", "-Xlint:preview");
    }

    @Test
    void extractsLegacyMavenCompilerArgumentText() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "maven-java21-preview-legacy");

        var metadata = new MavenProjectInspector().inspect(fixturePath);

        assertThat(metadata.compilerArgs()).containsExactly("--enable-preview", "-Xlint:preview");
    }

    @Test
    void extractsCompilerArgsFromVisibleMavenCompilerPluginExecution() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "maven-java21-preview-execution");

        var metadata = new MavenProjectInspector().inspect(fixturePath);

        assertThat(metadata.compilerArgs()).containsExactly("--enable-preview");
    }

    @Test
    void rejectsPathsWithoutAPomXmlFile() {
        var missingPath = Path.of("src", "test", "resources", "fixtures", "missing-pom");

        assertThatThrownBy(() -> new MavenProjectInspector().inspect(missingPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No pom.xml found");
    }
}
