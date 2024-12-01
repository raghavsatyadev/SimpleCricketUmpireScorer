buildscript {
    dependencies {
        classpath(libs.com.google.firebase)
        classpath(libs.androidx.navigation)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    alias(libs.plugins.google.gms) apply false

    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false

    alias(libs.plugins.room) apply false

    alias(libs.plugins.org.sonarqube)
}