package clickstream.health.time

public interface CSHealthTimeStampGenerator {
    public fun getTimeStamp(): Long
}

public class DefaultCSHealthTimeStampGenerator(
    private val timestampListener: CSEventGeneratedTimestampListener
) : CSHealthTimeStampGenerator {

    override fun getTimeStamp(): Long {
        return runCatching {
            timestampListener.now()
        }.getOrDefault(System.currentTimeMillis())
    }
}
