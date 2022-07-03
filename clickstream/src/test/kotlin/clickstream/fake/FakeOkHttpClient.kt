package clickstream.fake

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

internal fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()
}