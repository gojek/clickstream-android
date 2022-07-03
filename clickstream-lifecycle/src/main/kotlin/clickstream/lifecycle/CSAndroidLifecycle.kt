package clickstream.lifecycle

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import clickstream.lifecycle.internal.CSActivityResumedLifecycle
import clickstream.lifecycle.internal.CSAppResumedLifecycle
import clickstream.lifecycle.internal.CSConnectivityOnLifecycle
import clickstream.lifecycle.internal.CSLifecycleOwnerResumedLifecycle
import clickstream.lifecycle.internal.CSServiceStartedLifecycle
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object CSAndroidLifecycle {
    public const val APPLICATION_THROTTLE_TIMEOUT_MILLIS: Long = 10_000L
    private const val ACTIVITY_THROTTLE_TIMEOUT_MILLIS = 500L

    @JvmStatic
    @JvmOverloads
    public fun ofActivityForeground(
        application: Application,
        logger: CSLogger,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSActivityResumedLifecycle(application, logger, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(logger, application))

    @JvmStatic
    @JvmOverloads
    public fun ofApplicationForeground(
        application: Application,
        logger: CSLogger,
        lifecycleRegistry: LifecycleRegistry
    ): Lifecycle =
        CSAppResumedLifecycle(logger, lifecycleRegistry)
            .combineWith(CSConnectivityOnLifecycle(logger, application))

    @JvmStatic
    @JvmOverloads
    public fun ofLifecycleOwnerForeground(
        application: Application,
        lifecycleOwner: LifecycleOwner,
        logger: CSLogger,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSLifecycleOwnerResumedLifecycle(lifecycleOwner, logger, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(logger, application))

    @JvmStatic
    @JvmOverloads
    public fun ofServiceStarted(
        application: Application,
        lifecycleOwner: LifecycleOwner,
        logger: CSLogger,
        throttleTimeoutMillis: Long = ACTIVITY_THROTTLE_TIMEOUT_MILLIS
    ): Lifecycle =
        CSServiceStartedLifecycle(lifecycleOwner, logger, LifecycleRegistry(throttleTimeoutMillis))
            .combineWith(CSConnectivityOnLifecycle(logger, application))
}