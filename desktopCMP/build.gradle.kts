import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(projects.composeApp)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.logback.classic)
    }
  }
}

compose.desktop {
  application {
    mainClass = libs.versions.nameSpace.get() + ".MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = libs.versions.apkName.get()
      packageVersion = libs.versions.versionName.get()
    }
  }
}
