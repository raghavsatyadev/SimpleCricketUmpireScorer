@file:Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
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

include(":app", ":support", ":composeApp", ":androidCMP")
