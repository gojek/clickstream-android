package clickstream.api


import clickstream.health.proto.HealthMeta.Location
import clickstream.health.proto.HealthMeta.Customer
import clickstream.health.proto.HealthMeta.App
import clickstream.health.proto.HealthMeta.Device
import clickstream.health.proto.HealthMeta.Session


/**
 * This data source is responsible for providing values for those common keys
 * so that the can be added to each event before being passed to the scheduler.
 */
public interface CSMetaProvider {

    /**
     * Fetches and returns values for location properties.
     *
     * @return [Location]
     */
    public suspend fun location(): Location

    /**
     * Fetches and returns values for customer properties.
     *
     * @return [Customer]
     */
    public val customer: Customer

    /**
     * Fetches and returns values for app properties.
     *
     * @return [App]
     */
    public val app: App

    /**
     * Fetches and returns values for device properties.
     *
     * @return [Device]
     */
    public val device: Device

    /**
     * Fetches and returns values for current session related properties.
     *
     * @return [Session]
     */
    public val session: Session
}
