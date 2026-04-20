package dev.modernjava.upgrade.rewrite;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class OpenRewriteSuggestionServiceTest {

    @Test
    void suggestsJava21RecipeForTarget21() {
        var suggestions = new OpenRewriteSuggestionService().suggestForTarget(21);

        assertThat(suggestions)
                .extracting(OpenRewriteSuggestion::recipe)
                .contains("org.openrewrite.java.migrate.UpgradeToJava21");
    }

    @Test
    void rendersMavenCommandForOpenRewriteSuggestion() {
        var suggestion = new OpenRewriteSuggestion(
                "Upgrade Java to 21",
                "org.openrewrite.java.migrate.UpgradeToJava21");

        var command = suggestion.mavenCommand();
        var expectedCommand = "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run "
                + "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE "
                + "-Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21 "
                + "-Drewrite.exportDatatables=true";

        assertThat(command).isEqualTo(expectedCommand);
    }

    @Test
    void returnsEmptyListForUnsupportedTarget() {
        var suggestions = new OpenRewriteSuggestionService().suggestForTarget(19);

        assertThat(suggestions).isEqualTo(List.of());
    }
}
