package io.github.raghavsatyadev.scus.support.di

import io.github.raghavsatyadev.scus.ui.create_match.CreateMatchScreenViewModel
import io.github.raghavsatyadev.scus.ui.dashboard.DashboardScreenViewModel
import io.github.raghavsatyadev.scus.ui.main.MainViewModel
import io.github.raghavsatyadev.scus.ui.match_complete.MatchCompleteScreenViewModel
import io.github.raghavsatyadev.scus.ui.match_record.MatchRecordScreenViewModel
import io.github.raghavsatyadev.scus.ui.user.LoginScreenViewModel
import io.github.raghavsatyadev.support.di.supportModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
  includes(supportModule)

  viewModelOf(::MainViewModel)
  viewModelOf(::CreateMatchScreenViewModel)
  viewModelOf(::DashboardScreenViewModel)
  viewModelOf(::MatchCompleteScreenViewModel)
  viewModelOf(::MatchRecordScreenViewModel)
  viewModelOf(::LoginScreenViewModel)
}
