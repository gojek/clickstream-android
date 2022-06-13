package com.clickstream.app.helper

import android.util.Log
import com.clickstream.app.BuildConfig

fun printMessage(message: () -> String): Unit {
    if (BuildConfig.DEBUG) {
        Log.d("ClickStreamApp", message())
    }
}