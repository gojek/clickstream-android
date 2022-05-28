package clickstream.config

import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit.SECONDS
import okhttp3.OkHttpClient

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
 * @param initialRetryDurationInMs
 *        Initial retry duration to be used for retry backoff strategy (in milliseconds)
 * @param maxConnectionRetryDurationInMs
 *        Maximum retry duration for retry backoff strategy (in milliseconds)
 * @param maxRetriesPerBatch Maximum retries per batch request
 * @param maxRequestAckTimeout Maximum timeout for a request to receive Ack (in milliseconds)
 * @param okHttpClient OkHttpClient instance that passed from client
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
    @SerializedName("okHttpClient") val okHttpClient: OkHttpClient
) {

    public companion object {

        /**
         * Helper method to create instance of NetworkConfiguration with default values
         */
        public fun default(okHttpClient: OkHttpClient): CSNetworkConfig = CSNetworkConfig(
            "",
            SECONDS.toSeconds(CONNECT_TIMEOUT),
            SECONDS.toSeconds(READ_TIMEOUT),
            SECONDS.toSeconds(WRITE_TIMEOUT),
            SECONDS.toSeconds(PING_INTERVAL),
            SECONDS.toMillis(INITIAL_RETRY_DURATION),
            SECONDS.toMillis(MAX_RETRY_DURATION),
            MIN_BATTERY_LEVEL,
            MAX_RETRIES_PER_BATCH,
            SECONDS.toMillis(MAX_REQUEST_ACK_TIMEOUT),
            okHttpClient
        )
    }
}
