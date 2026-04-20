package dev.modernjava.upgrade.core;

import java.util.List;

@FunctionalInterface
public interface MigrationRule {
    List<Finding> evaluate(RuleContext context);
}
