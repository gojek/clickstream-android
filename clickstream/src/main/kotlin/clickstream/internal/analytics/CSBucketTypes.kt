package clickstream.internal.analytics

internal object CSBucketTypes {
    // Batch Latency
    const val LT_1sec_2G = "2G_LT_1sec"
    const val LT_1sec_3G = "3G_LT_1sec"
    const val LT_1sec_4G = "4G_LT_1sec"
    const val LT_1sec_WIFI = "WIFI_LT_1sec"
    const val MT_1sec_2G = "2G_MT_1sec"
    const val MT_1sec_3G = "3G_MT_1sec"
    const val MT_1sec_4G = "4G_MT_1sec"
    const val MT_1sec_WIFI = "WIFI_MT_1sec"
    const val MT_3sec_2G = "2G_MT_3sec"
    const val MT_3sec_3G = "3G_MT_3sec"
    const val MT_3sec_4G = "4G_MT_3sec"
    const val MT_3sec_WIFI = "WIFI_MT_3sec"

    // Batch Size
    const val LT_10KB = "LT_10KB"
    const val MT_10KB = "MT_10KB"
    const val MT_20KB = "MT_20KB"
    const val MT_50KB = "MT_50KB"

    // Event and Event Batch Wait Time
    const val LT_5sec = "LT_5sec"
    const val LT_10sec = "LT_10sec"
    const val MT_10sec = "MT_10sec"
    const val MT_20sec = "MT_20sec"
}