package clickstream.internal.eventprocessor

import com.gojek.clickstream.internal.HealthMeta.App
import com.gojek.clickstream.internal.HealthMeta.Customer
import com.gojek.clickstream.internal.HealthMeta.Device
import com.gojek.clickstream.internal.HealthMeta.Location
import com.gojek.clickstream.internal.HealthMeta.Session

/**
 * This data source is responsible for providing values for those common keys
 * so that the can be added to each event before being passed to the scheduler.
 */
internal interface CSMetaProvider {

    /**
     * Fetches and returns values for location properties.
     *
     * @return [Location]
     */
    suspend fun location(): Location

    /**
     * Fetches and returns values for customer properties.
     *
     * @return [Customer]
     */
    val customer: Customer

    /**
     * Fetches and returns values for app properties.
     *
     * @return [App]
     */
    val app: App

    /**
     * Fetches and returns values for device properties.
     *
     * @return [Device]
     */
    val device: Device

    /**
     * Fetches and returns values for current session related properties.
     *
     * @return [Session]
     */
    val session: Session
}
