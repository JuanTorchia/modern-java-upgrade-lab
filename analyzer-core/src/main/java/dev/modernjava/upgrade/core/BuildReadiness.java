package dev.modernjava.upgrade.core;

public record BuildReadiness(
        boolean buildWrapperPresent,
        String ciProvider,
        String ciEvidence,
        String suggestedTestCommand) {

    public static BuildReadiness unknown(String buildTool) {
        return new BuildReadiness(false, null, null, defaultTestCommand(buildTool, false));
    }

    public static String defaultTestCommand(String buildTool, boolean wrapperPresent) {
        if ("gradle".equalsIgnoreCase(buildTool)) {
            return wrapperPresent ? "./gradlew test" : "gradle test";
        }
        if ("maven".equalsIgnoreCase(buildTool)) {
            return wrapperPresent ? "./mvnw test" : "mvn test";
        }
        return "Run the project test suite";
    }
}
