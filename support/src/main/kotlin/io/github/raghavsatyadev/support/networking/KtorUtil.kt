package io.github.raghavsatyadev.support.networking

import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.extensions.serializer.kotlinJsonSerializer
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

object KtorUtil {
    @Volatile
    var httpClient: HttpClient
        private set

    init {
        httpClient = HttpClient(Android) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    kotlinJsonSerializer
                )
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(HttpRequestRetry) {
                // function enables retrying a request if a 5xx response is received from a server and specifies the number of retries.
                retryOnServerErrors(3)
                // specifies an exponential delay between retries, which is calculated using the Exponential backoff algorithm.
                exponentialDelay()
                // If you want to add some additional params in header
                modifyRequest { request ->
                    request.headers.append("x-retry-count", 2.toString())
                }
            }
            install(HttpCache) {
                val cacheFile = File(StorageUtils.getCacheDirectory(), "networking").apply {
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