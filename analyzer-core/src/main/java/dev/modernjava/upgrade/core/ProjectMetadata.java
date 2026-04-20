package dev.modernjava.upgrade.core;

import java.util.List;

public record ProjectMetadata(
        String buildTool,
        String declaredJavaVersion,
        String springBootVersion,
        List<String> dependencies) {
}
