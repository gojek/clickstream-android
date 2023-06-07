package clickstream.internal.db.impl

import android.content.Context
import clickstream.internal.db.CSBatchSizeSharedPref
import kotlinx.coroutines.coroutineScope

private const val CLICKSTREAM_BATCH_PREF = "Clickstream_Batch_Pref"
private const val BATCH_SIZE_PREF_KEY = "batch_size"
private const val DEFAULT_BATCH_SIZE = 250

internal class DefaultCSBatchSizeSharedPref(
    private val context: Context
) : CSBatchSizeSharedPref {

    override suspend fun saveBatchSize(batchSize: Int) {
        coroutineScope {
            val sharedPref =
                context.getSharedPreferences(CLICKSTREAM_BATCH_PREF, Context.MODE_PRIVATE)
            val finalBatchSize = when (batchSize < DEFAULT_BATCH_SIZE) {
                true -> DEFAULT_BATCH_SIZE
                false -> batchSize
            }
            with(sharedPref.edit()) {
                putInt(BATCH_SIZE_PREF_KEY, finalBatchSize)
                apply()
            }
        }
    }

    override suspend fun getSavedBatchSize(): Int {
        return coroutineScope {
            val sharedPref = context.getSharedPreferences(CLICKSTREAM_BATCH_PREF, Context.MODE_PRIVATE)
            sharedPref.getInt(BATCH_SIZE_PREF_KEY, DEFAULT_BATCH_SIZE)
        }
    }
}