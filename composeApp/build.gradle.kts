import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

kotlin {
  android {
    namespace = libs.versions.sharedAndroidId.get()
    compileSdk = libs.versions.compileSdk.get().toInt()
    androidResources.enable = true
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
  }

  jvm("desktop")

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "SCUS"
      isStatic = true
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.compose.ui.preview)
      implementation(libs.compose.activity)
    }

    commonMain.dependencies {
      implementation(libs.bundles.compose.mp)
      implementation(libs.bundles.koin.mp)
      implementation(libs.bundles.coil.mp)
      implementation(libs.bundles.lifecycle.mp)
      implementation(libs.bundles.navigation.mp)
      implementation(libs.bundles.room.mp)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
    }
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.ui.tooling)
  add("kspAndroid", libs.room.compiler)
  add("kspDesktop", libs.room.compiler)
  add("kspIosSimulatorArm64", libs.room.compiler)
  add("kspIosArm64", libs.room.compiler)
}
