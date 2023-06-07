package clickstream.health.time


/***
 * To be implemented by client to provide timestamp.
 *
 * */
public interface CSHealthTimeStampGenerator {
    public fun getTimeStamp(): Long
}