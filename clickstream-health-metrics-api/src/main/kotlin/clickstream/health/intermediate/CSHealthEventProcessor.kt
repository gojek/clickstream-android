package clickstream.health.intermediate

import androidx.annotation.RestrictTo
import clickstream.health.proto.Health

/**
 * [CSHealthEventProcessor] is the Heart of the Clickstream Library. The [CSHealthEventProcessor]
 * is only for pushing events to the backend. [CSHealthEventProcessor] is respect to the
 * Application lifecycle where on the active state, we have a ticker that will collect events from database
 * and the send that to the backend. The ticker will run on every 10seconds and will be stopped
 * whenever application on the inactive state.
 *
 * On the inactive state we will running flush for both Events and HealthEvents, where
 * it would be transformed and send to the backend.
 *
 * **Sequence Diagram**
 * ```
 *            App                               Clickstream
 * +---+---+---+---+---+---+           +---+---+---+---+---+---+
 * |     Sending Events    | --------> |  Received the Events  |
 * +---+---+---+---+---+---+           +---+---+---+---+---+---+
 *                                                 |
 *                                                 |
 *                                                 |                         +---+---+---+---+---+---+---+---+----+
 *                                         if app on active state ---------> |   - run the ticker with 10s delay  |
 *                                                 |                         |   - collect events from db         |
 *                                                 |                         |   - transform and send to backend  |
 *                                                 |                         +---+---+---+---+---+---+---+---+----+
 *                                                 |
 *                                                 |                         +---+---+---+---+---+---+---+---+---+---+----+
 *                                         else if app on inactive state --> |   - run flushEvents and flushHealthMetrics |
 *                                                                           |   - transform and send to backend          |
 *                                                                           +---+---+---+---+---+---+---+---+---+----+---+
 *```
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSHealthEventProcessor {
    public suspend fun getAggregateEvents(): List<Health>

    public suspend fun getInstantEvents(): List<Health>
}
