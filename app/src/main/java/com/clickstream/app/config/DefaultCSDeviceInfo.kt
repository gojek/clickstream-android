package com.clickstream.app.config

import clickstream.config.CSDeviceInfo

fun CSDeviceInfo() = object : CSDeviceInfo {
    override fun getDeviceManufacturer(): String = "Google"

    override fun getDeviceModel(): String = "Pixel 4"

    override fun getSDKVersion(): String = "1"

    override fun getOperatingSystem(): String = "Android P"

    override fun getDeviceHeight(): String = "1080"

    override fun getDeviceWidth(): String = "720"
}