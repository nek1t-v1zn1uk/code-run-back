plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "coderun"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // WebMVC
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    // Kotlin modules
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Sprint Boot Dev Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // Tests
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // HTTP Client
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    // JPA Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")
    // Flyway
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
