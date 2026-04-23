plugins {
    java
    id("org.springframework.boot") version "3.3.5"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.spring.boot.starter.test)
}
