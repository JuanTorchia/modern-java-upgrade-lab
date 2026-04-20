package dev.modernjava.upgrade.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RuleEngine {

    private final List<MigrationRule> rules;

    public RuleEngine(List<MigrationRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public List<Finding> evaluate(RuleContext context) {
        Objects.requireNonNull(context, "context");
        var findings = new ArrayList<Finding>();
        for (MigrationRule rule : rules) {
            findings.addAll(rule.evaluate(context));
        }
        return List.copyOf(findings);
    }
}
