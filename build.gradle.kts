import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("org.flywaydb.flyway") version "9.8.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

group = "com.mobimeo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

val jdbc = configurations.create("jdbc")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("io.projectreactor.netty:reactor-netty")

    // test containers for postgres
    testImplementation("org.testcontainers:r2dbc:1.17.0")
    testImplementation("org.testcontainers:testcontainers:1.17.0")
    testImplementation("org.testcontainers:postgresql:1.17.0")
    testImplementation("org.testcontainers:junit-jupiter:1.17.0")
    testImplementation("org.flywaydb:flyway-core:9.16.0")
    testImplementation("io.rest-assured:rest-assured:5.3.0")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    jdbc("org.postgresql:postgresql:42.5.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

flyway {
    configurations = arrayOf("jdbc")
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
    baselineOnMigrate = true
}
