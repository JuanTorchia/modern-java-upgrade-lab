package dev.modernjava.upgrade.core;

public record Finding(
        String id,
        FindingSeverity severity,
        String area,
        String title,
        String evidence,
        String recommendation,
        String openRewriteRecipe) {
}
