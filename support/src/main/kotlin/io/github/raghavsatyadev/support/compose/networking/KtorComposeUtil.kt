package io.github.raghavsatyadev.support.compose.networking

import io.github.raghavsatyadev.support.compose.storage.StorageComposeUtils
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.kotlinJsonSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorProvider @Inject constructor(val storageUtils: StorageComposeUtils) {
  val httpClient: HttpClient by lazy {
    HttpClient(Android) {
      expectSuccess = true
      install(ContentNegotiation) { json(kotlinJsonSerializer) }
      install(Logging) { level = LogLevel.BODY }
      install(HttpRequestRetry) {
        retryOnServerErrors(3)
        exponentialDelay()
        modifyRequest { request -> request.headers.append("x-retry-count", 2.toString()) }
      }
      install(HttpCache) {
        val cacheFile =
          File(storageUtils.getCacheDirectory(), "networking").apply {
            mkdirs()
            if (!exists()) {
              createNewFile()
            }
          }
        val storage = FileStorage(cacheFile)
        privateStorage(storage)
      }
      install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 10000
        socketTimeoutMillis = 10000
      }
    }
  }
}
