plugins {
    java
    alias(libs.plugins.missing.plugin)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.missing.library)
}
