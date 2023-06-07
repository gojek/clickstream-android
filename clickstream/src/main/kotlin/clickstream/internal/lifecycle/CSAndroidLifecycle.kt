package clickstream.internal.lifecycle

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import clickstream.internal.lifecycle.internal.CSActivityResumedLifecycle
import clickstream.internal.lifecycle.internal.CSAppResumedLifecycle
import clickstream.internal.lifecycle.internal.CSConnectivityOnLifecycle
import clickstream.internal.lifecycle.internal.CSLifecycleOwnerResumedLifecycle
import clickstream.internal.lifecycle.internal.CSServiceStartedLifecycle
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

/**
 * An Collection of ClickStream Android Lifecycle
 */
public object CSAndroidLifecycle {
    private const val APPLICATION_THROTTLE_TIMEOUT_MILLIS = 10_000L
    private const val ACTIVITY_THROTTLE_TIMEOUT_MILLIS = 500L

    @JvmStatic
    @JvmOverloads
    public fun ofActivityForeground(
        application: Application,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSActivityResumedLifecycle(application, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(application))

    @JvmStatic
    @JvmOverloads
    public fun ofApplicationForeground(
        application: Application,
        throttleTimeoutMillis: Long = APPLICATION_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSAppResumedLifecycle(LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(application))

    @JvmStatic
    @JvmOverloads
    public fun ofLifecycleOwnerForeground(
        application: Application,
        lifecycleOwner: LifecycleOwner,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSLifecycleOwnerResumedLifecycle(lifecycleOwner, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(application))

    @JvmStatic
    @JvmOverloads
    public fun ofServiceStarted(
        application: Application,
        lifecycleOwner: LifecycleOwner,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSServiceStartedLifecycle(lifecycleOwner, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(application))
}