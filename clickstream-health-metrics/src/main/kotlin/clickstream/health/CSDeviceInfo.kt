package clickstream.health

/**
 * Fetches and returns device details.
 */
public interface CSDeviceInfo {

    /**
     * Returns device manufacturer
     */
    public fun getDeviceManufacturer(): String

    /**
     * Returns device model
     */
    public fun getDeviceModel(): String

    /**
     * Returns SDK version
     */
    public fun getSDKVersion(): String

    /**
     * Returns deviceManufacturer
     */
    public fun getOperatingSystem(): String

    /**
     * Returns deviceManufacturer
     */
    public fun getDeviceHeight(): String

    /**
     * Returns deviceManufacturer
     */
    public fun getDeviceWidth(): String
}
