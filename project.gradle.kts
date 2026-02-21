buildscript { dependencies { classpath(libs.com.google.firebase) } }

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.multiplatform.library) apply false
  alias(libs.plugins.compose.multiplatform) apply false

  alias(libs.plugins.google.plugin) apply false

  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.parcelize) apply false

  alias(libs.plugins.compose.compiler) apply false

  alias(libs.plugins.stability.analyzer) apply false

  alias(libs.plugins.room) apply false

  alias(libs.plugins.sonarqube)

  alias(libs.plugins.kotzilla) apply false
}
