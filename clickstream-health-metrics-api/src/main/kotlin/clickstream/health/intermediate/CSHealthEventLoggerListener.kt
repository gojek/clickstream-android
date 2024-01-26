package clickstream.health.intermediate

import clickstream.health.constant.CSHealthKeysConstant

/**
 *
 * Listener to observe Health events during background flush. Client app needs to
 * provide an implementation for this. To be used from [CSHealthEventProcessor.getHealthEventFlow].
 *
 * This can be useful if client wants to track clickstream health events on some
 * third party service (like firebase) and monitor clickstream health.
 *
 * Flow is as follows:
 *
 * App goes to background -> Events flush -> Health event flush -> notify client app about health events
 * via [CSHealthEventLoggerListener].
 *
 */
public interface CSHealthEventLoggerListener {

    /**
     * Method called to notify client app.
     * @param eventName
     * @param healthData, Check [CSHealthKeysConstant] for keys.
     */
    public fun logEvent(eventName: String, healthData: HashMap<String, Any>)
}
