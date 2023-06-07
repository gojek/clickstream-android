package clickstream.config

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.SECONDS

private const val CONNECT_TIMEOUT = 10L
private const val READ_TIMEOUT = 10L
private const val WRITE_TIMEOUT = 10L
private const val PING_INTERVAL = 5L
private const val INITIAL_RETRY_DURATION = 1L
private const val MAX_RETRY_DURATION = 60L
private const val MIN_BATTERY_LEVEL = 10
private const val MAX_RETRIES_PER_BATCH = 20
private const val MAX_REQUEST_ACK_TIMEOUT = 10L

/**
 * Data class which will be used to configure timeouts for network channel.
 *
 * @param endPoint Endpoint for web socket server
 * @param connectTimeout Connect timeout to be used by okhtp (in seconds)
 * @param readTimeout Read timeout to be used by okhttp (in seconds)
 * @param writeTimeout Write timeout to be used by okhttp (in seconds)
 * @param pingInterval Interval between pings initiated by client (in seconds)
 * @param initialRetryDurationInMs Initial retry duration to be used for retry backoff strategy (in milliseconds)
 * @param maxConnectionRetryDurationInMs Maximum retry duration for retry backoff strategy (in milliseconds)
 * @param maxRetriesPerBatch Maximum retries per batch request
 * @param maxRequestAckTimeout Maximum timeout for a request to receive Ack (in milliseconds)
 */
public data class CSNetworkConfig(
    @SerializedName("endPoint") val endPoint: String,
    @SerializedName("connectTimeout") val connectTimeout: Long,
    @SerializedName("readTimeout") val readTimeout: Long,
    @SerializedName("writeTimeout") val writeTimeout: Long,
    @SerializedName("pingInterval") val pingInterval: Long,
    @SerializedName("initialRetryDurationInMs") val initialRetryDurationInMs: Long,
    @SerializedName("maxConnectionRetryDurationInMs") val maxConnectionRetryDurationInMs: Long,
    @SerializedName("minBatteryLevel") val minBatteryLevel: Int,
    @SerializedName("maxRetriesPerBatch") val maxRetriesPerBatch: Int,
    @SerializedName("maxRequestAckTimeout") val maxRequestAckTimeout: Long,
    @SerializedName("headers") val headers: Map<String, String> = mapOf(),
    @SerializedName("okHttpClient") val okHttpClient: OkHttpClient? = null
) {

    public companion object {
        /**
         * A default configuration for CSNetworkConfig
         */
        public fun default(
            url: String,
            headers: Map<String, String>,
            okHttpClient: OkHttpClient? = null
        ): CSNetworkConfig =
            CSNetworkConfig(
                endPoint = url,
                connectTimeout = SECONDS.toSeconds(CONNECT_TIMEOUT),
                readTimeout = SECONDS.toSeconds(READ_TIMEOUT),
                writeTimeout = SECONDS.toSeconds(WRITE_TIMEOUT),
                pingInterval = SECONDS.toSeconds(PING_INTERVAL),
                initialRetryDurationInMs = SECONDS.toMillis(INITIAL_RETRY_DURATION),
                maxConnectionRetryDurationInMs = SECONDS.toMillis(MAX_RETRY_DURATION),
                minBatteryLevel = MIN_BATTERY_LEVEL,
                maxRetriesPerBatch = MAX_RETRIES_PER_BATCH,
                maxRequestAckTimeout = SECONDS.toMillis(MAX_REQUEST_ACK_TIMEOUT),
                headers = headers,
                okHttpClient = okHttpClient,
            )
    }
}
