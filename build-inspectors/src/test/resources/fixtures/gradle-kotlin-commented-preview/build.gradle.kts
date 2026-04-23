plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// options.compilerArgs.add("--enable-preview")
/*
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}
*/
