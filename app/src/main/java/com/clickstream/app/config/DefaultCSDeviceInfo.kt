package com.clickstream.app.config

import clickstream.api.CSDeviceInfo

fun csDeviceInfo() = object : CSDeviceInfo {
    override fun getDeviceManufacturer(): String = "Google"

    override fun getDeviceModel(): String = "Pixel 4"

    override fun getSDKVersion(): String = "1"

    override fun getOperatingSystem(): String = "Android P"

    override fun getDeviceHeight(): String = "1080"

    override fun getDeviceWidth(): String = "720"
}