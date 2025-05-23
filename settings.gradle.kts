@file:Suppress("UnstableApiUsage")

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
    maven(url = "https://central.maven.org/maven2/")
      maven { url = uri("https://androidx.dev/snapshots/builds/13508953/artifacts/repository") }
  }
}

rootProject.buildFileName = "project.gradle.kts"

rootProject.name = "SCUS"

include(":app", ":support")
