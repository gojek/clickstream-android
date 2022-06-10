package com.clickstream.app.config

import android.util.Base64
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient

inline class AccountId(val value: String)
inline class SecretKey(val value: String)
inline class EndPoint(val value: String)
inline class StubBearer(val value: String)

internal object CSNetworkModule {

    fun create(
        accountId: AccountId,
        secretKey: SecretKey,
        stubBearer: StubBearer
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(createInterceptor(accountId, secretKey, stubBearer))
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .pingInterval(1, TimeUnit.SECONDS)
            .build()
    }

    private fun createInterceptor(
        accountId: AccountId,
        secretKey: SecretKey,
        stubBearer: StubBearer
    ): Interceptor {
        return Interceptor { chain ->
            val value = "$accountId:$secretKey"
            val encoded = Base64.encode(value.toByteArray(), Base64.NO_WRAP)

            // override Authorization Bearer
            val keyToValue = mapOf(
                "Authorization" to "Bearer ${stubBearer.value}"
            )
            val request = chain.request().newBuilder()
                .apply {
                    keyToValue.forEach { (k, v) ->
                        header(k, v)
                    }
                }
                .build()
            chain.proceed(request)
        }
    }
}