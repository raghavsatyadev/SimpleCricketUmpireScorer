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
        maven(url = "https://jitpack.io")
        maven(url = "https://central.maven.org/maven2/")
        maven(url = "https://jitpack.io")
    }
}
rootProject.buildFileName = "project.gradle.kts"

rootProject.name = "SimpleCricketUmpireScorer"
include(":app", ":support")