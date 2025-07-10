@file:Suppress("DEPRECATION")

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)

  alias(libs.plugins.google.gms)

  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)

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

fun readProperties(propertiesFile: File) = Properties().apply {
  propertiesFile
    .inputStream()
    .use { fis -> load(fis) }
}

val props = readProperties(file("../secret.properties"))

android {
  namespace = libs.versions.nameSpace.get()
  compileSdk = libs.versions.compileSdk
    .get()
    .toInt()
  // compileSdkPreview = libs.versions.compileSdkPreview.get()
  buildToolsVersion = libs.versions.buildTools.get()
  defaultConfig {
    applicationId = libs.versions.appIdDev.get()
    minSdk = libs.versions.minSdk
      .get()
      .toInt()
    targetSdk = libs.versions.targetSdk
      .get()
      .toInt()
    // targetSdkPreview = libs.versions.targetSdkPreview.get()
    versionCode = libs.versions.versionCode
      .get()
      .toInt()
    versionName = libs.versions.versionName.get()
    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  signingConfigs {
    create("release") {
      props
        .getProperty("storeFile")
        ?.let { storeFile = file(it) }
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
        listOf(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          file("proguard-rules.pro")
        )
      )
      signingConfig = signingConfigs.getByName("release")
      //            applicationIdSuffix = ".beta"
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles.addAll(
        listOf(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          file("proguard-rules.pro")
        )
      )
      signingConfig = signingConfigs.getByName("release")

      ndk {
        abiFilters.clear()
        abiFilters.addAll(
          listOf(
            "armeabi-v7a",
            "arm64-v8a"
          )
        )
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
  val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

  androidComponents.beforeVariants { variant ->
    val names = variant.flavorName

    if ((names == "Dev" && variant.buildType == "release") || (names == "Prod" && (variant.buildType != "release"))) {
      variant.enable = false
    }
  }
  applicationVariants.configureEach {
    val variant = this
    if (!variant.name
        .lowercase(Locale.getDefault())
        .contains("debug")
    ) {
      outputs.configureEach {
        val output = this
        renameOutputs(
          variant,
          output
        )
      }
    }
  }
}

fun getBuildName(variantName: String): String {
  val timestamp = SimpleDateFormat("dd-MM-yy_HH-mm").format(Date())

  return "${libs.versions.apkName.get()}-${variantName}-${timestamp}"
}

fun moveAAB(
  variant: ApplicationVariant,
  outputFullName: String,
  buildOutputDirectory: File,
  buildTypeDirectory: File,
) {
  val name = variant.name
  val variantNameCapitalized = name.replaceFirstChar { it.uppercase() }
  val bundleTaskName = "bundle${variantNameCapitalized}"
  val bundleTask = tasks.named(bundleTaskName)

  val copyBundleTask = tasks.register<Copy>("copy${variantNameCapitalized}Bundle") {
    dependsOn(bundleTask)
    bundleTask
      .get()
      .doLast {
        println("Copying AAB to output directory: $buildOutputDirectory")
        copy {
          val aabFile = buildTypeDirectory
            .walkTopDown()
            .find { it.name.endsWith(".aab") }
          print("AAB Location: ${aabFile?.absolutePath ?: "EMPTY"}")
          from(aabFile!!)
          into(buildOutputDirectory)
          rename { "$outputFullName.aab" }
        }
        println("Deleting build type directory: $buildTypeDirectory")
        buildTypeDirectory.parentFile?.deleteRecursively()
      }
    }
  bundleTask.configure { finalizedBy(copyBundleTask) }
}

fun moveAPK(
  variant: ApplicationVariant,
  buildOutputDirectoryPath: String,
  output: BaseVariantOutput,
  outputFullName: String,
  variantName: String?,
  buildTypeDirectory: File,
) {
  variant.assembleProvider
    .get()
    .doLast {
      println("Copying APK to output directory: $buildOutputDirectoryPath")
      copy {
        from(output.outputFile.absolutePath)
        into(buildOutputDirectoryPath)
        rename { "${outputFullName}.apk" }
      }

      println("Copying mapping file to output directory: $buildOutputDirectoryPath")
      copy {
        from(variant.mappingFileProvider.get())
        into(buildOutputDirectoryPath)
        rename { "${outputFullName}.txt" }
      }

      val nativeSymbolsDir = file(
        "${layout.buildDirectory.asFile}/app/intermediates/merged_native_libs/${variantName}/out/lib"
      )

      if (nativeSymbolsDir.exists()) {
        println(
          "Zipping native debug symbols and copying them to output directory: $buildOutputDirectoryPath"
        )

        val zipFile = file("$buildOutputDirectoryPath/$outputFullName-native_debug_symbols.zip")

        ant.invokeMethod(
          "zip",
          mapOf(
            "destfile" to zipFile,
            "basedir" to nativeSymbolsDir
          )
        )
      }

      println("Deleting build type directory: $buildTypeDirectory")
      buildTypeDirectory.parentFile?.deleteRecursively()
    }
}

fun renameOutputs(
  variant: ApplicationVariant,
  output: BaseVariantOutput,
): BaseVariantOutput {
  val variantName = variant.name

  if (variantName
      .lowercase()
      .contains("release")
  ) {
    val outputFullName = getBuildName(variantName)
    val buildTypeDirectory = output.outputFile.parentFile
    val buildOutputDirectory = buildTypeDirectory?.parentFile?.parentFile
    val buildOutputDirectoryPath = buildOutputDirectory?.path
    buildOutputDirectory?.mkdirs()

    if (buildOutputDirectoryPath != null) {
      moveAAB(
        variant,
        outputFullName,
        buildOutputDirectory,
        buildTypeDirectory
      )
      moveAPK(
        variant,
        buildOutputDirectoryPath,
        output,
        outputFullName,
        variantName,
        buildTypeDirectory,
      )
    }
  }
  return output
}

dependencies {
  implementation(project(path = ":support"))

  // Kotlin
  implementation(libs.bundles.kotlin)

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

  // Room
  implementation(libs.room)
  implementation(libs.room.runtime)
  ksp(libs.room.compiler)

  // WorkManager
  implementation(libs.androidx.work.runtime)

  // Test
  testImplementation(libs.bundles.test)
  androidTestImplementation(libs.bundles.androidTest)

  // Others
  implementation(libs.bundles.other)

  implementation(libs.androidx.multidex)
  coreLibraryDesugaring(libs.desugar.jdk.libs)
}
