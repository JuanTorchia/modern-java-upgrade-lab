package dev.modernjava.upgrade.core;

import java.util.List;
import java.util.Objects;

public record RiskAssessment(RiskLevel level, int score, List<String> reasons) {

    public RiskAssessment {
        level = Objects.requireNonNull(level, "level");
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
        if (reasons.isEmpty()) {
            reasons = List.of("No migration risk signals crossed the scoring threshold");
        }
    }
}
