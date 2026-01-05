# Koin vs Hilt:

This document serves as a learning resource to understand the architectural and practical differences between Hilt (built on Dagger) and Koin, two of the most popular Dependency Injection (DI) frameworks in the Kotlin ecosystem.

## 1. Core: Annotations vs. Pure Kotlin

The fundamental difference lies in how dependencies are defined and resolved.

### **Hilt (Annotation & Code Gen Based)**
Hilt is a strict, compile-time dependency injection framework. It uses annotations (`@Inject`, `@Module`, `@HiltAndroidApp`) to generate Java code that connects your dependencies.
*   **Pros:** Compile-time safety (errors are caught before running the app).
*   **Cons:** Slower build times due to annotation processing (KAPT/KSP). Requires annotations in your functional code.

### **Koin (DSL & Runtime Based)**
Koin is a lightweight service locator framework that uses a Kotlin Domain Specific Language (DSL).
*   **Pros:** fast build times, no code generation, powerful runtime features, and highly idiomatic Kotlin syntax.
*   **Cons:** Errors (like missing definitions) are typically caught at runtime.
    *   **Mitigation:** We have implemented a `checkModules` test in `app/src/test/kotlin/io/github/raghavsatyadev/scus/di/CheckModulesTest.kt` which runs `appModule.verify()` during unit tests to ensure the graph is valid at compile/check time.

---

## 2. Koin: DSL vs. Annotations

Interestingly, Koin now offers two ways to define dependencies. This is often a point of confusion.

### A. Koin DSL (The "Classic" & Recommended Way)
You explicitly declare modules and definitions using Kotlin code.

```kotlin
val myModule = module {
    singleOf(::RepositoryImpl)
    factoryOf(::UserUseCase)
}
```

*   **Pros:** Zero build-time overhead. "Magic-free" code—you can see exactly where everything is defined. 100% KMP compatible out of the box.
*   **Cons:** You must manually register modules.

### B. Koin Annotations (Compiler Plugin)
Uses KSP to generate the module definitions for you, similar to Hilt.

```kotlin
@Single
class RepositoryImpl : Repository

@Factory
class UserUseCase(val repo: Repository)
```

*   **Pros:** Feels very familiar to Hilt/Dagger developers. Auto-generates module definitions. Offers some compile-time safety.
*   **Cons:** Adds build time (KSP step). Hides the "wiring" logic.

### **Which is "Better"?**
**The DSL approach is generally considered better** for true Kotlin usage because:
1.  **Build Speed:** It stays significantly faster as projects grow.
2.  **Multiplatform:** It is the most robust way to share code across platforms without worrying about KSP compiler plugin compatibility variations.
3.  **Refactoring:** Renaming classes doesn't break string-based lookups or generated code references as easily.

---

## 3. Implementation Comparisons

### A. Singleton Definition
**Hilt:** Requires the `@Singleton` annotation or an `@InstallIn` scope.
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppDataModule {
    @Provides
    @Singleton
    fun provideDataRepo(): DataRepo = DataRepoImpl()
}
```

**Koin:** Uses `single` or `singleOf`.
```kotlin
val appModule = module {
    // simple
    singleOf(::DataRepoImpl) 
}
```

### B. Named/Qualified Injections (Handling Multiple Types)
Sometimes you need two instances of the same class (e.g., a "Auth" Retrofit service and a "Public" Retrofit service).

**Hilt:** Requires creating custom "Qualifier" annotations.
```kotlin
// 1. Define Annotation
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

// 2. Provide
@Module
...
@Provides
@AuthRetrofit
fun provideAuthRetrofit(): Retrofit = ...

// 3. Inject
class AuthService @Inject constructor(
    @AuthRetrofit private val retrofit: Retrofit
)
```

**Koin:** Uses simple String qualifiers or Type qualifiers (named modules).
```kotlin
val networkModule = module {
    // 1. Provide with "named"
    single(named("Auth")) { createAuthRetrofit() }
    single(named("Public")) { createPublicRetrofit() }
}

class AuthService(private val retrofit: Retrofit)

val serviceModule = module {
    // 2. Inject by name
    single { AuthService(get(named("Auth"))) }
}
```
*Note how Koin handles this inline without creating extra annotation classes.*

### C. Scoping (LifeCycle Management)
**Hilt:** Scopes are rigid and predefined (`SingletonComponent`, `ActivityComponent`, `ViewModelComponent`, `FragmentComponent`). Creating custom scopes is possible but verbose.

**Koin:** Scopes are flexible and string-based. You can create a scope for *anything* (a session, a wizard flow, a specific screen).
```kotlin
val sessionModule = module {
    scope(named("UserSession")) {
        scopedOf(::SessionManager)
    }
}
```

---

## 4. Why Koin Wins for Kotlin Multiplatform (KMP)

This is the decisive factor for modern development.
*   **Hilt:** heavily relies on Android-specific lifecycle classes (`Application`, `Activity`). It does not run on iOS or Desktop natively without significant workarounds or sticking to strict JVM modules.
*   **Koin:** Platform agnostic. The exact same module `val appModule = module { ... }` runs on:
    *   Android
    *   iOS (Kotlin Native)
    *   Desktop (JVM)
    *   Web (Wasm)
    
    For a shared UI (Compose Multiplatform) project, Koin allows you to share your ViewModels and Data layers 100% in the `commonMain` source set.

## Summary Checklist

| Feature | Hilt | Koin |
| :--- | :--- | :--- |
| **Boilerplate** | High (Annotations, Components, Modules) | Low (DSL) |
| **Named Dependencies** | Complex (Custom Annotations) | Simple (Strings/Enum) |
| **Build Impact** | Slow (KAPT/KSP) | Instant |
| **Learning Curve** | High (Dagger complexity) | Low (Intuitive DSL) |
| **KMP Ready** | No | **Yes** |
