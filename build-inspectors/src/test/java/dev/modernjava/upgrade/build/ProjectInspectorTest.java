package dev.modernjava.upgrade.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectInspectorTest {

    @TempDir
    Path tempDir;

    @Test
    void selectsMavenWhenPomExists() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "maven-java8-springboot2");

        var metadata = new ProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("maven");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("8");
    }

    @Test
    void selectsGradleWhenGradleBuildExists() {
        var fixturePath = Path.of("src", "test", "resources", "fixtures", "gradle-kotlin-java21-springboot3");

        var metadata = new ProjectInspector().inspect(fixturePath);

        assertThat(metadata.buildTool()).isEqualTo("gradle");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("21");
    }

    @Test
    void prefersMavenWhenBothBuildFilesExist() throws Exception {
        Files.writeString(tempDir.resolve("pom.xml"), """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>example</groupId>
                  <artifactId>demo</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <java.version>17</java.version>
                  </properties>
                </project>
                """);
        Files.writeString(tempDir.resolve("build.gradle.kts"), """
                plugins {
                    java
                }

                java {
                    toolchain {
                        languageVersion.set(JavaLanguageVersion.of(21))
                    }
                }
                """);

        var metadata = new ProjectInspector().inspect(tempDir);

        assertThat(metadata.buildTool()).isEqualTo("maven");
        assertThat(metadata.declaredJavaVersion()).isEqualTo("17");
    }

    @Test
    void rejectsUnknownBuildTool() {
        assertThatThrownBy(() -> new ProjectInspector().inspect(tempDir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No Maven or Gradle build file found");
    }
}
