package clickstream.internal.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope

private const val MAX_BATTERY_LEVEL = 100
private const val HALF_BATTERY_LEVEL = 50f

/**
 * This is being used to monitor battery status and provide callback to network manager in case of battery changes
 *
 * @param context Application context
 * @param minBatteryLevel Minimum battery level below which socket conection would be closed
 */
@ExperimentalCoroutinesApi
internal class CSBatteryStatusObserver(
    private val context: Context,
    private val minBatteryLevel: Int
) {

    /**
     * Will be used by the listener to get current state of a resource
     *
     * @return [CSBatteryLevel] which represents the current status of the battery
     */
    internal suspend fun getBatteryStatus(): CSBatteryLevel {
        return coroutineScope {
            val batteryStatusIntent: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                    context.registerReceiver(null, filter)
                }
            val deviceStatus: Int =
                batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPercent: Float = batteryStatusIntent?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * MAX_BATTERY_LEVEL / scale.toFloat()
            } ?: HALF_BATTERY_LEVEL

            when {
                deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING -> CSBatteryLevel.CHARGING
                deviceStatus == BatteryManager.BATTERY_STATUS_FULL -> CSBatteryLevel.ADEQUATE_POWER
                batteryPercent < minBatteryLevel -> CSBatteryLevel.LOW_BATTERY
                else -> CSBatteryLevel.ADEQUATE_POWER
            }
        }
    }
}
