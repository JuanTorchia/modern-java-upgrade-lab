package dev.modernjava.upgrade.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuleEngineTest {

    @Test
    void runsRulesInDeclaredOrder() {
        var metadata = new ProjectMetadata("maven", "8", "2.7.18", List.of(), List.of());
        var context = new RuleContext(new AnalysisRequest(java.nio.file.Path.of("."), 17), metadata);
        MigrationRule firstRule = ruleContext -> List.of(new Finding(
                "first",
                FindingCategory.BASELINE,
                FindingSeverity.INFO,
                "Test",
                "First finding",
                "First evidence",
                "First recommendation",
                null));
        MigrationRule secondRule = ruleContext -> List.of(new Finding(
                "second",
                FindingCategory.FRAMEWORK,
                FindingSeverity.RISK,
                "Test",
                "Second finding",
                "Second evidence",
                "Second recommendation",
                null));

        var findings = new RuleEngine(List.of(firstRule, secondRule)).evaluate(context);

        assertThat(findings)
                .extracting(Finding::id)
                .containsExactly("first", "second");
    }

    @Test
    void returnsNoFindingsWhenNoRulesExist() {
        var metadata = new ProjectMetadata("maven", "17", null, List.of(), List.of());
        var context = new RuleContext(new AnalysisRequest(java.nio.file.Path.of("."), 17), metadata);

        var findings = new RuleEngine(List.of()).evaluate(context);

        assertThat(findings).isEmpty();
    }
}
