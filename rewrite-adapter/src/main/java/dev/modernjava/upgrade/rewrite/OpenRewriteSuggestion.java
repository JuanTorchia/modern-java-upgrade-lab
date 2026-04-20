package dev.modernjava.upgrade.rewrite;

import java.util.Objects;

public class OpenRewriteSuggestion {

    private final String title;
    private final String recipe;

    public OpenRewriteSuggestion(String title, String recipe) {
        this.title = Objects.requireNonNull(title, "title");
        this.recipe = Objects.requireNonNull(recipe, "recipe");
    }

    public String title() {
        return title;
    }

    public String recipe() {
        return recipe;
    }

    public String mavenCommand() {
        return mavenCommand(recipe);
    }

    public static String mavenCommand(String recipe) {
        Objects.requireNonNull(recipe, "recipe");
        return "mvn -U org.openrewrite.maven:rewrite-maven-plugin:run "
                + "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE "
                + "-Drewrite.activeRecipes=" + recipe + " "
                + "-Drewrite.exportDatatables=true";
    }
}
