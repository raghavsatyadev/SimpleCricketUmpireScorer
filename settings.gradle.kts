@file:Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven(url = "https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

rootProject.buildFileName = "project.gradle.kts"

rootProject.name = "SCUS"

include(":app", ":support", ":composeApp", ":androidCMP", ":desktopCMP")
