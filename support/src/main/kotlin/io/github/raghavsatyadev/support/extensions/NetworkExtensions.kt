package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.github.raghavsatyadev.support.R

@Suppress("MemberVisibilityCanBePrivate", "unused")
object NetworkExtensions {

  /** checks for network immediately and returns true if connected */
  private fun Context.isInternetAvailableLocal(): Boolean {
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

  fun Context.isInternetAvailable(view: View? = null): Boolean {
    val isConnected = isInternetAvailableLocal()
    view?.let {
      if (!isConnected) {
        Handler(Looper.getMainLooper()).post {
          Snackbar.make(it, R.string.warning_connect_internet, Snackbar.LENGTH_LONG).show()
        }
      }
    }
    return isConnected
  }

  /**
   * checks for network immediately and returns true if connected
   *
   * @param view shows snack bar anchored to given view if no network is available
   */
  fun Fragment.isInternetAvailable(view: View? = null) = requireContext().isInternetAvailable(view)
}
