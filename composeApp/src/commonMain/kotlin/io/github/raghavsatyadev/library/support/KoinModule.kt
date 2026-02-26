package io.github.raghavsatyadev.library.support

import io.github.raghavsatyadev.library.support.database.commonDatabaseModule
import io.github.raghavsatyadev.library.support.database.platformDatabaseModule
import io.kotzilla.generated.monitoring
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
  // Add your modules here
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  startKoin {
    appDeclaration()
    modules(appModule, commonDatabaseModule, platformDatabaseModule())
    monitoring()
  }
}
