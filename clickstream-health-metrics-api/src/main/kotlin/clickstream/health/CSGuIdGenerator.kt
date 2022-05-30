package clickstream.health

import java.util.UUID

public interface CSGuIdGenerator {
    public fun getId(): String
}

public class CSGuIdGeneratorImpl : CSGuIdGenerator {
    override fun getId(): String {
        return UUID.randomUUID().toString()
    }
}
