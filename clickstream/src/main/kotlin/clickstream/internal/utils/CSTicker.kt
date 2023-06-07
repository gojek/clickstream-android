package clickstream.internal.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Creates a flow that produces the first item after
 * the given initial delay and subsequent items with the
 * given delay between them.
 *
 * This channel stops producing elements immediately after [Flow.collect] invocation.
 *
 * @param delayMillis delay between each element in milliseconds.
 * @param initialDelay delay after which the first element
 * will be produced (it is equal to [delayMillis] by default) in milliseconds.
 */
@ExperimentalCoroutinesApi
internal fun flowableTicker(
    delayMillis: Long,
    initialDelay: Long = delayMillis
): Flow<Unit> = callbackFlow {
    delay(initialDelay)
    while (!this.isClosedForSend) {
        send(Unit)
        delay(delayMillis)
    }
    awaitClose {}
}