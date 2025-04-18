buildscript {
  dependencies {
    classpath(libs.com.google.firebase)
    classpath(libs.androidx.navigation)
  }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false

  alias(libs.plugins.google.plugin) apply false

  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.parcelize) apply false

  alias(libs.plugins.compose.plugin) apply false

  alias(libs.plugins.hilt) apply false

  alias(libs.plugins.room) apply false

  alias(libs.plugins.sonarqube)
}
