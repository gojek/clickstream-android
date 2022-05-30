package clickstream.health.identity

import java.util.UUID

public interface CSGuIdGenerator {
    public fun getId(): String
}

public class DefaultCSGuIdGenerator : CSGuIdGenerator {
    override fun getId(): String {
        return UUID.randomUUID().toString()
    }
}
