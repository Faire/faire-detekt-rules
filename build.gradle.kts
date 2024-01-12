plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`

    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

group = "com.faire.detektrules"
version = "0.1.0"

val detektVersion = "1.23.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("compiler-embeddable"))
    runtimeOnly(kotlin("reflect"))

    implementation(libs.detekt.api)

    runtimeOnly(libs.detekt.formatting)

    testImplementation(libs.assertj)
    testImplementation(libs.detekt.test)
    testImplementation(libs.guava)
    testImplementation(libs.junit.jupiter.api)

    testRuntimeOnly(libs.junit.jupiter.engine)

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:$detektVersion")
    detektPlugins("com.braisgabin.detekt:kotlin-compiler-wrapper:0.0.4")
    detektPlugins(rootProject)
}

detekt {
    parallel = true
    autoCorrect = true

    buildUponDefaultConfig = true
    config.from(rootProject.file("detekt.yaml"))

    allRules = false // activate all available (even unstable) rules.
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}