package io.github.raghavsatyadev.scus.di

import io.github.raghavsatyadev.scus.support.di.appModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        appModule.verify(
            extraTypes = listOf(
                android.content.Context::class,
                androidx.work.WorkerParameters::class,
            )
        )
    }
}
