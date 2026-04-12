package io.github.raghavsatyadev.library.support

import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.database.commonDatabaseModule
import io.github.raghavsatyadev.library.support.database.platformDatabaseModule
import io.github.raghavsatyadev.library.support.google.repository.AuthRepository
import io.github.raghavsatyadev.library.support.google.repository.DummyAuthRepository
import io.github.raghavsatyadev.library.ui.create_match.CreateMatchScreenViewModel
import io.github.raghavsatyadev.library.ui.dashboard.DashboardScreenViewModel
import io.github.raghavsatyadev.library.ui.main.MainViewModel
import io.github.raghavsatyadev.library.ui.match_complete.MatchCompleteScreenViewModel
import io.github.raghavsatyadev.library.ui.match_record.MatchRecordScreenViewModel
import io.github.raghavsatyadev.library.ui.user.LoginScreenViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
  singleOf(::UiStateManager)
  single<AuthRepository> { DummyAuthRepository() }

  viewModelOf(::MainViewModel)
  viewModelOf(::CreateMatchScreenViewModel)
  viewModelOf(::DashboardScreenViewModel)
  viewModelOf(::MatchCompleteScreenViewModel)
  viewModelOf(::MatchRecordScreenViewModel)
  viewModelOf(::LoginScreenViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  startKoin {
    appDeclaration()
    modules(appModule, commonDatabaseModule, platformDatabaseModule())
  }
}
