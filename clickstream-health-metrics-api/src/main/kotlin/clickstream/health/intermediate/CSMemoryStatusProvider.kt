package clickstream.health.intermediate

/**
 * Class responsible to provide current RAM status of device. Client needs to implement this.
 * This is used inside [CSHealthEventProcessor] to check memory before processing health events.
 *
 * */
public interface CSMemoryStatusProvider {

    /**
     * @return true if memory is under pressure.
     */
    public fun isLowMemory(): Boolean
}