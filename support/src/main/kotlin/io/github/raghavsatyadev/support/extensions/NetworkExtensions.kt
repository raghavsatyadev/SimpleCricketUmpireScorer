package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat

@Suppress("MemberVisibilityCanBePrivate", "unused")
object NetworkExtensions {

  /** checks for network immediately and returns true if connected */
  fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = ContextCompat.getSystemService(this, ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    val isConnected =
      when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
      }
    return isConnected
  }
}
