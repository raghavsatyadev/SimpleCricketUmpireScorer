package io.github.raghavsatyadev.support.compose.networking

import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KtorModule {
  @Provides
  @Singleton
  fun provideHttpClient(provider: KtorProvider): HttpClient = provider.httpClient
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface KtorEntryPoint {
  fun httpClient(): HttpClient
}
