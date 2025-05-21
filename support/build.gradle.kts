import java.util.Properties

plugins {
  alias(libs.plugins.android.library)

  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)

  alias(libs.plugins.compose.plugin)

  alias(libs.plugins.hilt)

  alias(libs.plugins.safe.args)

  alias(libs.plugins.room)
}

fun readProperties(propertiesFile: File) =
  Properties().apply { propertiesFile.inputStream().use { fis -> load(fis) } }

val props = readProperties(file("../secret.properties"))

android {
  namespace = libs.versions.supportId.get()
  compileSdk = libs.versions.compileSdk.get().toInt()
  // compileSdkPreview = libs.versions.compileSdkPreview.get()
  buildToolsVersion = libs.versions.buildTools.get()

  room {
    schemaDirectory("$projectDir/schemas")
    // incremental("true")
    // generateKotlin("true")
  }
  ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
  }

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles.add(file("consumer-rules.pro"))

    sourceSets { getByName("androidTest").assets.srcDirs(files("$projectDir/schemas")) }

    ndk { abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a")) }

    props.entries.forEach { (key, value) ->
      val keyString = key.toString()
      if (keyString.startsWith("res_")) {
        resValue("string", keyString.replace("res_", ""), value.toString())
      }
    }
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
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      resValue("string", "app_name", libs.versions.betaAppName.get())
      signingConfig = signingConfigs.getByName("release")
    }
    getByName("release") {
      isMinifyEnabled = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      resValue("string", "app_name", libs.versions.releaseAppName.get())
      signingConfig = signingConfigs.getByName("release")
    }
    getByName("debug") {
      isMinifyEnabled = false
      resValue("string", "app_name", libs.versions.debugAppName.get())
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
      create("Prod") {}
      create("Dev") {}
    }
    androidComponents.beforeVariants { variant ->
      val names = variant.flavorName

      if (
        (names == "Dev" && variant.buildType == "release") ||
          (names == "Prod" && (variant.buildType != "release"))
      ) {
        variant.enable = false
      }
    }
    packaging {
      jniLibs {
        pickFirsts.addAll(
          listOf(
            "lib/*/libc++_shared.so",
            "lib/*/libgnustl_shared.so",
            "lib/*/libyuv.so",
            "lib/*/libopenh264.so",
          )
        )
      }
    }
  }
}

dependencies {
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
