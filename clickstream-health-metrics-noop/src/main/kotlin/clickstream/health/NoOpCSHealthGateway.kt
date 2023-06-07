package clickstream.health

import clickstream.health.intermediate.CSHealthEventProcessor

public object NoOpCSHealthGateway {

    public fun factory(): CSHealthGateway {
        return object : CSHealthGateway {

            override val healthEventProcessor: CSHealthEventProcessor? = null

            override suspend fun clearHealthEventsForVersionChange() {
                /*NoOp*/
            }

        }
    }
}
