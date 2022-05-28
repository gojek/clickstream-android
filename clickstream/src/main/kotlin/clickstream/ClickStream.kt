package clickstream

import clickstream.ClickStream.Companion.getInstance
import clickstream.ClickStream.Companion.initialize
import clickstream.config.CSConfiguration
import clickstream.internal.DefaultClickStream
import clickstream.internal.DefaultClickStream.Companion.initialize
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * An entry point to [initialize] and [getInstance] of the [ClickStream]. In order to use
 * [ClickStream] you should call [initialize] before call [getInstance]. Calling [getInstance]
 * before [initialize] will throw an [IllegalStateException].
 *
 * Both [initialize] and [getInstance] are thread-safe, means those functions are able to spawned
 * by multiple-thread. Internal [ClickStream] utilize lazy initialization where it would
 * contribute less amount of time on the [Application#onCreate], however if in certain cases that
 * [ClickStream] take some amount of time during app launch, we
 * do suggest to run [initialize] and [getInstance] in the background thread.
 *
 * **Example:**
 * ```kotlin
 *
 * // initialization
 * ClickStream.initialize(context, CSConfiguration.Builder()..build())
 *
 * // get the instance
 * val clickstream = ClickStream.getInstance()
 * ```
 */
public interface ClickStream {

    /**
     * Push an event with a set of attribute pairs.
     *
     * @param event a [CSEvent] to be send.
     * @param expedited a flag to determine whether [CSEvent] should be sent expedited.
     */
    public fun trackEvent(event: CSEvent, expedited: Boolean)

    @ExperimentalCoroutinesApi
    public companion object {

        /**
         * Retrieves the singleton instance of [DefaultClickStream].
         *
         * @return The singleton instance of [ClickStream].
         * @throws IllegalStateException if the instance is null.
         */
        public fun getInstance(): ClickStream {
            val clickStream = DefaultClickStream.getInstance()
            if (clickStream == null) {
                throw IllegalStateException(
                    "ClickStream is not initialized properly. " +
                    "The most likely cause is that your " +
                    "forgot to call ClickStream#initialize in your Application#onCreate. "
                )
            } else {
                return clickStream
            }
        }

        /**
         * Initializes the singleton instance of [DefaultClickStream]. You should only call [initialize]
         * one time. [initialize] can be called on the different thread.
         *
         * @param configuration The [CSConfiguration] for used to set up [ClickStream].
         *
         * @throws IllegalStateException if [initialize] is called multiple times
         */
        public fun initialize(configuration: CSConfiguration) {
            DefaultClickStream.initialize(configuration)
        }

        /**
         * Release [DefaultClickStream] instance.
         *
         * This function is necessary to be called before [initialize] [ClickStream] if
         * [CSConfiguration] changed.
         */
        public fun release() {
            DefaultClickStream.release()
        }
    }
}
