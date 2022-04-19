package com.clickstream.app.helper

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import java.io.InputStreamReader
import java.util.Date

val gson by lazy {
    GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
        .registerTypeAdapter(Date::class.java, DateTypeAdapter())
        .create()
}

inline fun <reified T> Class<T>.load(file: String): T? {
    val fixtureStreamReader = InputStreamReader(classLoader!!.getResourceAsStream(file))
    return gson.fromJson(fixtureStreamReader, this)
}
