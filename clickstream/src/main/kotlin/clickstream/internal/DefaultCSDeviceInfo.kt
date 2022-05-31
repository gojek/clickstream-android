package clickstream.internal

import android.content.res.Resources
import android.os.Build
import clickstream.api.CSDeviceInfo

/**
 * Implementation of [CSDeviceInfo]
 */
internal class DefaultCSDeviceInfo : CSDeviceInfo {

    override fun getDeviceManufacturer(): String {
        return Build.MANUFACTURER
    }

    override fun getDeviceModel(): String {
        return Build.MODEL
    }

    override fun getSDKVersion(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    override fun getOperatingSystem(): String {
        return "android"
    }

    override fun getDeviceHeight(): String {
        return Resources.getSystem().displayMetrics.heightPixels.toString()
    }

    override fun getDeviceWidth(): String {
        return Resources.getSystem().displayMetrics.widthPixels.toString()
    }
}
