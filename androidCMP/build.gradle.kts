import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

kotlin { jvmToolchain(21) }

val secretProps =
  Properties().apply {
    rootProject.file("secret.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
  }

android {
  signingConfigs {
    create("release") {
      secretProps.getProperty("storeFile")?.let { storeFile = rootProject.file("app/$it") }
      storePassword = secretProps.getProperty("storePassword")
      keyAlias = secretProps.getProperty("keyAlias")
      keyPassword = secretProps.getProperty("keyPassword")
    }
  }
  buildTypes { getByName("release") { signingConfig = signingConfigs.getByName("release") } }
  namespace = libs.versions.nameSpace.get()
  compileSdk { version = release(libs.versions.compileSdk.get().toInt()) { minorApiLevel = 1 } }
  buildToolsVersion = libs.versions.buildTools.get()

  defaultConfig {
    applicationId = libs.versions.appIdProd.get()
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = libs.versions.versionCode.get().toInt()
    versionName = libs.versions.versionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    resValue("string", "app_name", libs.versions.releaseAppName.get())
  }
  buildFeatures { resValues = true }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
  implementation(projects.composeApp)
  implementation(libs.bundles.android.mp)
  debugImplementation(libs.compose.ui.tooling)
  // debugImplementation(libs.bundles.update.libs)
}
