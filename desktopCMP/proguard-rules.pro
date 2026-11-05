# Desktop release: ProGuard shrinks only (no obfuscation or optimization, see build.gradle.kts), on
# top of Compose's default rules. Unlike R8 on Android it does not read the libraries' own rules,
# so libraries that load classes by name, through ServiceLoader or from native code are kept whole.

# --- Our code: Koin, kotlinx.serialization, Room and Nav3 reach parts of it by name. ---
-keep class io.github.raghavsatyadev.scus.** { *; }

# --- Native code calls back into these. ---
-keep class androidx.sqlite.** { *; }
-keep class org.jetbrains.skiko.** { *; }
-keep class org.jetbrains.skia.** { *; }

# --- Shrinking drops interfaces that default methods call through (VerifyError in JobSupport). ---
-keep class kotlinx.coroutines.** { *; }

# --- Loaded by name or through ServiceLoader. ---
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class ch.qos.logback.** { *; }
-keep class org.slf4j.** { *; }
-keep class coil3.** { *; }
-keep class androidx.room3.** { *; }
-keep class androidx.room.** { *; }
-keep class * extends androidx.room3.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class org.koin.** { *; }

# --- kotlinx.serialization: serializers are found through the companion's serializer(). ---
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod, RuntimeVisibleAnnotations
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static ** Companion;
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer { *; }
-keep class kotlinx.serialization.** { *; }

# --- Optional parts of libraries the app never reaches (servlets, Android, GraalVM). ---
-dontwarn ch.qos.logback.**
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn org.slf4j.**
-dontwarn coil3.**
-dontwarn javax.annotation.**
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.**
