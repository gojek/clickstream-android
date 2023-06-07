package clickstream.health.intermediate

import androidx.annotation.RestrictTo
import clickstream.health.model.CSHealthEventDTO

/**
 * [CSHealthEventRepository] Act as repository pattern where internally it doing DAO operation
 * to insert, delete, and read the [CSHealthEvent]'s.
 *
 * If you're using `com.gojek.clickstream:clickstream-health-metrics-noop`, the
 * [CSHealthEventRepository] internally will doing nothing.
 *
 * Do consider to use `com.gojek.clickstream:clickstream-health-metrics`, to operate
 * [CSHealthEventRepository] as expected. Whenever you opt in the `com.gojek.clickstream:clickstream-health-metrics`,
 * you should never touch the [DefaultCSHealthEventRepository] explicitly. All the wiring
 * is happening through [DefaultCSHealthGateway.factory(/*args*/)]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSHealthEventRepository {

    /**
     * A function to insert the health event into the DB
     */
    public suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO)

    /**
     * A function to insert the health event list into the DB
     */
    public suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>)

    /**
     * A function to retrieve all the bucket health events in the DB
     */
    public suspend fun getBucketEvents(): List<CSHealthEventDTO>

    /**
     * A function to retrieve all the instant health events in the DB
     */

    public suspend fun getInstantEvents(): List<CSHealthEventDTO>

    /**
     * A function to retrieve all the aggregate health events in the DB
     */
    public suspend fun getAggregateEvents(): List<CSHealthEventDTO>

    /**
     * A function to delete all the health events for a sessionID
     */
    public suspend fun deleteHealthEventsBySessionId(sessionId: String)

    /**
     * A function to delete the given health events
     */
    public suspend fun deleteHealthEvents(events: List<CSHealthEventDTO>)
}
