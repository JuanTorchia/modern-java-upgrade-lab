package dev.modernjava.upgrade.rewrite;

import java.util.List;

public class OpenRewriteSuggestionService {

    public List<OpenRewriteSuggestion> suggestForTarget(int targetJavaVersion) {
        return switch (targetJavaVersion) {
            case 17 -> List.of(new OpenRewriteSuggestion(
                    "Upgrade Java to 17",
                    "org.openrewrite.java.migrate.UpgradeToJava17"));
            case 21 -> List.of(new OpenRewriteSuggestion(
                    "Upgrade Java to 21",
                    "org.openrewrite.java.migrate.UpgradeToJava21"));
            case 25 -> List.of(new OpenRewriteSuggestion(
                    "Upgrade Java to 25",
                    "org.openrewrite.java.migrate.UpgradeToJava25"));
            default -> List.of();
        };
    }
}
