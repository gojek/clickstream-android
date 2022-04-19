package com.clickstream.app.helper

import android.util.Log
import com.clickstream.app.BuildConfig

fun print(message: () -> String) {
    if (BuildConfig.DEBUG) {
        Log.d("ClickStream", message())
    }
}