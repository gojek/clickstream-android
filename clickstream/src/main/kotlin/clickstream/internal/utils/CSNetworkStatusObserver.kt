package clickstream.internal.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.coroutineScope

/**
 * his is being used to monitor connectivity status and provide callback to network manager in case of network changes
 */
internal class CSNetworkStatusObserver(
    private val context: Context
) {

    /**
     * Check whether the device has active internet and return boolean
     *
     * @return false - if device has no internet
     * @return true - if device has active internet
     */
    internal suspend fun isNetworkAvailable(): Boolean {
        return coroutineScope {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw = connectivityManager.activeNetwork ?: return@coroutineScope false
                val actNw =
                    connectivityManager.getNetworkCapabilities(nw) ?: return@coroutineScope false
                return@coroutineScope when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    // for other device how are able to connect with Ethernet
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    // for check internet over Bluetooth
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    else -> false
                }
            } else {
                val nwInfo = connectivityManager.activeNetworkInfo ?: return@coroutineScope false
                return@coroutineScope nwInfo.isConnected
            }
        }
    }
}
