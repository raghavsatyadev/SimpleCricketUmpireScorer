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
    // `packageReleaseMsi`: ProGuard shrinks only; obfuscation and optimization break reflection.
    buildTypes.release.proguard {
      version.set("7.10.0")
      configurationFiles.from(project.file("proguard-rules.pro"))
      obfuscate.set(false)
      optimize.set(false)
    }
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = libs.versions.apkName.get()
      packageVersion = libs.versions.versionName.get()
      windows {
        menuGroup = libs.versions.apkName.get()
        shortcut = true
        perUserInstall = true
        // Fixed so a new MSI upgrades the installed app instead of adding a second copy.
        upgradeUuid = "960e9b3b-54ac-49b0-9d7e-5842e2a60f81"
      }
    }
  }
}

/**
 * ProGuard cannot read the Kotlin 2.5 stdlib's metadata yet. It only reads metadata when
 * `kotlin.Metadata` is kept, and Compose's default rules keep all of `kotlin.**`, so the release
 * build uses a copy of those rules without it. Same fix as JobFinder's desktopCMP.
 */
val composeRulesWithoutMetadata by tasks.registering {
  val unpacked =
    tasks.named("unpackDefaultComposeDesktopJvmApplicationResources").map {
      it.outputs.files.asFileTree.matching { include("**/default-compose-desktop-rules.pro") }
    }
  val out = layout.buildDirectory.file("compose/proguard/compose-rules.pro")
  inputs.files(unpacked)
  outputs.file(out)
  doLast {
    val original = unpacked.get().singleFile
    val text = original.readText()
    val keepKotlin = "-keep class kotlin.** { *; }"
    check(keepKotlin in text) { "Compose's ProGuard rules changed: recheck $original" }
    out
      .get()
      .asFile
      .writeText(text.replace(keepKotlin, "-keep class !kotlin.Metadata,kotlin.** { *; }"))
  }
}

// After the Compose plugin, which sets its own rules file in afterEvaluate.
afterEvaluate {
  tasks
    .withType<org.jetbrains.compose.desktop.application.tasks.AbstractProguardTask>()
    .configureEach {
      defaultComposeRulesFile.set(
        composeRulesWithoutMetadata.map { RegularFile { it.outputs.files.singleFile } }
      )
    }
}
