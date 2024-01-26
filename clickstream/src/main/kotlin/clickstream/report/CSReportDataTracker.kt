package clickstream.report

import clickstream.internal.eventscheduler.CSEventData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.collections.HashSet

/**
 * Helper class that tracks report related data using [CSReportDataListener]
 *
 * */
internal class CSReportDataTracker(private val csReportDataListener: CSReportDataListener) {

    private val uniqueIdSet = HashSet<String>()

    fun trackMessage(tag: String, message: String) {
        csReportDataListener.onNewData(
            tag, CSReportData.CSMessage(date = getDate(), message)
        )
    }

    suspend fun trackDupData(tag: String, eventData: List<CSEventData>) =
        withContext(Dispatchers.Default) {
            for (event in eventData) {
                if (uniqueIdSet.contains(event.eventGuid)) {
                    csReportDataListener.onNewData(
                        tag,
                        CSReportData.CSDuplicateEvents(
                            getDate(),
                            event.eventRequestGuid ?: "",
                            event.eventGuid,
                        )
                    )
                } else {
                    uniqueIdSet.add(event.eventGuid)
                }
            }
        }

    fun trackSuccess(tag: String, guid: String) {
        csReportDataListener.onNewData(
            tag, CSReportData.CSSuccess(getDate(), guid)
        )
    }

    fun trackFailure(tag: String, guid: String, exception: Throwable) {
        csReportDataListener.onNewData(
            tag, CSReportData.CSFailure(getDate(), guid, exception)
        )
    }

    private fun getDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        return formatter.format(calendar.time)
    }
}