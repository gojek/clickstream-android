package clickstream.health

import androidx.annotation.RestrictTo
import clickstream.health.intermediate.CSHealthEventProcessor

/**
 * Wrapper class that creates [CSHealthEventProcessor].
 *
 * */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSHealthGateway {

    /**
     * Class to process health events.
     *
     * */
    public val healthEventProcessor: CSHealthEventProcessor?

    /**
     * Clears health events on app version upgrade.
     *
     * */
    public suspend fun clearHealthEventsForVersionChange()

}