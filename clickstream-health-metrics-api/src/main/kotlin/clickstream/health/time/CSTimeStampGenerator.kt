package clickstream.health

import clickstream.health.time.CSEventGeneratedTimestampListener

public interface CSTimeStampGenerator {
    public fun getTimeStamp(): Long
}

public class DefaultCSTimeStampGenerator(
    private val timestampListener: CSEventGeneratedTimestampListener
) : CSTimeStampGenerator {

    override fun getTimeStamp(): Long {
        return runCatching {
            timestampListener.now()
        }.getOrDefault(System.currentTimeMillis())
    }
}
