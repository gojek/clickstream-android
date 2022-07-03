package clickstream.lifecycle.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSConnectivityOnLifecycle(
    private val logger: CSLogger,
    applicationContext: Context,
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry()
) : Lifecycle by lifecycleRegistry {

    init {
        logger.debug { "CSConnectivityOnLifecycle#init" }

        emitCurrentConnectivity(applicationContext)
        subscribeToConnectivityChange(applicationContext)
    }

    private fun emitCurrentConnectivity(applicationContext: Context) {
        logger.debug { "CSConnectivityOnLifecycle#emitCurrentConnectivity" }

        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        lifecycleRegistry.onNext(toLifecycleState(connectivityManager.isConnected()))
    }

    private fun subscribeToConnectivityChange(applicationContext: Context) {
        logger.debug { "CSConnectivityOnLifecycle#subscribeToConnectivityChange" }

        val intentFilter = IntentFilter()
            .apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        applicationContext.registerReceiver(ConnectivityChangeBroadcastReceiver(), intentFilter)
    }

    private fun ConnectivityManager.isConnected(): Boolean {
        val activeNetworkInfo = activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    private fun toLifecycleState(isConnected: Boolean): Lifecycle.State {
        logger.debug { "CSConnectivityOnLifecycle#toLifecycleState : isConnected $isConnected" }

        return if (isConnected) {
            Lifecycle.State.Started
        } else {
            Lifecycle.State.Stopped.AndAborted
        }
    }

    private inner class ConnectivityChangeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            logger.debug { "CSConnectivityOnLifecycle#ConnectivityChangeBroadcastReceiver#onReceive" }

            val extras = intent.extras ?: return
            val isConnected = !extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)
            lifecycleRegistry.onNext(toLifecycleState(isConnected))
        }
    }
}