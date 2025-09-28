import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)

  alias(libs.plugins.google.plugin)

  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)

  alias(libs.plugins.compose.plugin)

  alias(libs.plugins.hilt)

  alias(libs.plugins.crashlytics)

  alias(libs.plugins.safe.args)
}

sonar {
  properties {
    setAndroidVariant("DevDebug")
    property(
      "sonar.androidLint.reportPaths",
      "${layout.buildDirectory.asFile}/reports/lint-results-DevDebug.xml",
    )
  }
}

fun readProperties(propertiesFile: File) =
  Properties().apply { propertiesFile.inputStream().use { fis -> load(fis) } }

val props = readProperties(file("../secret.properties"))

android {
  namespace = libs.versions.nameSpace.get()
  compileSdk = libs.versions.compileSdk.get().toInt()
  // compileSdkPreview = libs.versions.compileSdkPreview.get()
  buildToolsVersion = libs.versions.buildTools.get()
  defaultConfig {
    applicationId = libs.versions.appIdDev.get()
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    // targetSdkPreview = libs.versions.targetSdkPreview.get()
    versionCode = libs.versions.versionCode.get().toInt()
    versionName = libs.versions.versionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  signingConfigs {
    create("release") {
      props.getProperty("storeFile")?.let { storeFile = file(it) }
      storePassword = props.getProperty("storePassword")
      keyAlias = props.getProperty("keyAlias")
      keyPassword = props.getProperty("keyPassword")
    }
  }
  buildTypes {
    create("beta") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      signingConfig = signingConfigs.getByName("release")
      //            applicationIdSuffix = ".beta"
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      signingConfig = signingConfigs.getByName("release")

      ndk {
        abiFilters.clear()
        abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
      }
    }
    getByName("debug") {
      isMinifyEnabled = false
      isShrinkResources = false
      //            applicationIdSuffix = ".debug"
      configure<CrashlyticsExtension> { mappingFileUploadEnabled = false }
    }
    kotlin { jvmToolchain(21) }
    compileOptions { isCoreLibraryDesugaringEnabled = true }
    buildFeatures {
      viewBinding = true
      buildConfig = true
      resValues = true
      compose = true
    }

    flavorDimensions.add("isPlayStoreVersion")
    productFlavors {
      create("Prod") {
        dimension = "isPlayStoreVersion"
        applicationId = libs.versions.appIdProd.get()
      }
      create("Dev") {
        dimension = "isPlayStoreVersion"
        applicationId = libs.versions.appIdDev.get()
      }
    }
  }

}

androidComponents {
  beforeVariants { variant ->
    val names = variant.flavorName

    val isDevRelease = names == "Dev" && variant.buildType == "release"
    val isProdNotRelease = names == "Prod" && (variant.buildType != "release")

    if (isDevRelease || isProdNotRelease) {
      variant.enable = false
    }
  }
  onVariants(selector().all()) { variant ->
    if (!variant.name.lowercase(Locale.getDefault()).contains("debug")) {
      project.renameOutputs(variant)
    }
  }
}

fun Project.renameOutputs(variant: ApplicationVariant) {
  if (!variant.name.lowercase(Locale.getDefault()).contains("release")) {
    return
  }

  if (!shouldCopyArtifacts()) {
    return
  }

  val outputFullName = getBuildName(variant.name)
  val buildOutputDirectory = layout.buildDirectory.dir("outputs")
  buildOutputDirectory.get().asFile.mkdirs()

  moveAAB(variant, outputFullName, buildOutputDirectory)
  moveAPK(variant, outputFullName, buildOutputDirectory)
}

fun getBuildName(variantName: String): String {
  val timestamp = SimpleDateFormat("dd-MM-yy_HH-mm").format(Date())

  return "${libs.versions.apkName.get()}-${variantName}-${timestamp}"
}

fun Project.moveAAB(
  variant: ApplicationVariant,
  outputFullName: String,
  buildOutputDirectory: Provider<Directory>,
) {
  val variantNameCapitalized = variant.name.replaceFirstChar { it.uppercase() }
  val bundleTaskName = "bundle${variantNameCapitalized}"
  val bundleTask = tasks.named(bundleTaskName)
  val bundleProvider = variant.artifacts.get(SingleArtifact.BUNDLE)

  val copyBundleTask =
    tasks.register("copy${variantNameCapitalized}Bundle") {
      doLast {
        val destination = buildOutputDirectory.get().asFile
        println("Copying AAB to output directory: $destination")
        val bundleFile = bundleProvider.orNull?.asFile
        if (bundleFile != null) {
          println("AAB Location: ${bundleFile.absolutePath}")
          this@moveAAB.copy {
            from(bundleFile)
            into(destination)
            rename { "$outputFullName.aab" }
          }
          println("Deleting build type directory: ${bundleFile.parentFile}")
          bundleFile.parentFile?.parentFile?.deleteRecursively()
        }
      }
    }
  bundleTask.configure { finalizedBy(copyBundleTask) }
}

fun Project.moveAPK(
  variant: ApplicationVariant,
  outputFullName: String,
  buildOutputDirectory: Provider<Directory>,
) {
  val variantNameCapitalized = variant.name.replaceFirstChar { it.uppercase() }
  val assembleTask = tasks.named("assemble${variantNameCapitalized}")
  val apkProvider = variant.artifacts.get(SingleArtifact.APK)
  val mappingProvider = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)

  val copyOutputsTask =
    tasks.register("copy${variantNameCapitalized}Outputs") {
      doLast {
        val destination = buildOutputDirectory.get().asFile
        val destinationPath = destination.path
        destination.mkdirs()
        println("Copying APK to output directory: $destinationPath")

        val apkFile = apkProvider.orNull?.asFile
        if (apkFile != null) {
          this@moveAPK.copy {
            from(apkFile)
            into(destinationPath)
            rename { "${outputFullName}.apk" }
          }

          println("Copying mapping file to output directory: $destinationPath")
          mappingProvider.orNull?.asFile?.let { mappingFile ->
            this@moveAPK.copy {
              from(mappingFile)
              into(destinationPath)
              rename { "${outputFullName}.txt" }
            }
          }

          val buildDirectory = layout.buildDirectory.get().asFile
          val nativeSymbolsDir =
            file("$buildDirectory/app/intermediates/merged_native_libs/${variant.name}/out/lib")

          if (nativeSymbolsDir.exists()) {
            println(
              "Zipping native debug symbols and copying them to output directory: $destinationPath"
            )

            val zipFile = file("$destinationPath/$outputFullName-native_debug_symbols.zip")

            ant.invokeMethod("zip", mapOf("destfile" to zipFile, "basedir" to nativeSymbolsDir))
          }

          println("Deleting build type directory: ${apkFile.parentFile}")
          apkFile.parentFile?.parentFile?.deleteRecursively()
        }
      }
    }

  assembleTask.configure { finalizedBy(copyOutputsTask) }
}

private fun Project.shouldCopyArtifacts(): Boolean {
  val taskNames = gradle.startParameter.taskNames
  if (taskNames.isEmpty()) {
    return false
  }

  return taskNames.any { task ->
    val normalized = task.substringAfterLast(':').lowercase(Locale.getDefault())
    normalized.startsWith("assemble") || normalized.startsWith("bundle")
  }
}

dependencies {
  implementation(project(path = ":support"))

  // Kotlin
  implementation(libs.bundles.kotlin)

  // Compose
  implementation(libs.bundles.compose)

  // Android
  implementation(libs.bundles.androidx)

  // LifeCycle
  implementation(libs.bundles.lifecycle)

  // Navigation
  implementation(libs.bundles.navigation)

  // Firebase
  implementation(libs.bundles.firebase)

  // Google
  implementation(libs.bundles.google)

  // Coil
  implementation(libs.bundles.coil)

  // Ktor
  implementation(libs.bundles.ktor)

  // Hilt
  implementation(libs.bundles.hilt)

  // Room
  implementation(libs.bundles.room)

  // KSP
  ksp(libs.bundles.ksp)

  // Test
  testImplementation(libs.bundles.test)
  androidTestImplementation(libs.bundles.androidTest)

  // Others
  implementation(libs.bundles.other)

  coreLibraryDesugaring(libs.desugar.jdk.libs)

  debugImplementation(libs.bundles.debug)
}
