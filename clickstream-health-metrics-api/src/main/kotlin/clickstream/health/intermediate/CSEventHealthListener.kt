package clickstream.health.intermediate

import androidx.annotation.RestrictTo
import clickstream.health.model.CSEventHealth

/**
 * [CSEventHealthListener] Essentially an optional listener which being used for
 * perform an analytic metrics to check every event size. We're exposed listener
 * so that if the host app wants to check each event size they can simply add the listener.
 *
 * Proto `MessageLite` provide an API that we're able to use to check the byte size which is
 * [messageSerializedSizeInBytes].
 *
 * **Example:**
 * ```kotlin
 * private fun applyEventHealthMetrics(config: CSClickStreamConfig): CSEventHealthListener {
 *     if (config.isEventHealthListenerEnabled.not()) return NoOpCSEventHealthListener()
 *
 *     return object : CSEventHealthListener {
 *         override fun onEventCreated(healthEvent: CSEventHealth) {
 *             executor.execute {
 *                 val trace = FirebasePerformance.getInstance().newTrace("CS_Event_Health_Metrics")
 *                 trace.start()
 *                 trace.putMetric(
 *                     healthEvent.messageName,
 *                     healthEvent.messageSerializedSizeInBytes.toLong()
 *                 )
 *                 trace.stop()
 *             }
 *        }
 *    }
 * }
 * ```
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSEventHealthListener {
    /**
     * [CSEventHealth] hold event meta information.
     */
    public fun onEventCreated(healthEvent: CSEventHealth)
}
