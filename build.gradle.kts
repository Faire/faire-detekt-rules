import com.vanniktech.maven.publish.SonatypeHost

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.maven.publishing)

  alias(libs.plugins.detekt)
}

group = "com.faire"
version = "0.2.5"

if (!providers.environmentVariable("RELEASE").isPresent) {
  val gitSha = providers.environmentVariable("GITHUB_SHA")
    .orElse(
      provider {
        // nest the provider, we don't want to invalidate the config cache for this
        providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }
          .standardOutput
          .asText
          .get()
      }
    )
    .get()

  version = "$version-$gitSha-SNAPSHOT"
}

val detektVersion = libs.versions.detekt.get()

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
  detektPlugins(libs.detekt.compiler.wrapper)
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

mavenPublishing {
  publishToMavenCentral(SonatypeHost.DEFAULT, true)
  signAllPublications()

  pom {
    name = "faire-detekt-rules"
    description = "Detekt rules for Faire"
    inceptionYear = "2023"
    url = "https://github.com/Faire/faire-detekt-rules"

    licenses {
      license {
        name = "The Apache License, Version 2.0"
        distribution = "repo"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }

    developers {
      developer {
        id = "Faire"
        name = "Faire Developers"
      }
    }

    organization {
      name = "Faire"
      url = "https://www.faire.com"
    }

    issueManagement {
      system = "GitHub"
      url = "https://github.com/Faire/faire-detekt-rules/issues"
    }

    scm {
      connection = "scm:git:git://github.com/Faire/faire-detekt-rules.git"
      url = "https://github.com/Faire/faire-detekt-rules"
    }
  }
}
