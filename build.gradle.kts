plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.faire.detektrules"
version = "0.0.1"

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
}


tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}