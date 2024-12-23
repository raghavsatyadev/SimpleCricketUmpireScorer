@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
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
    }
}
rootProject.buildFileName = "project.gradle.kts"
rootProject.name = "SimpleCricketUmpireScorer"
include(":app", ":support")