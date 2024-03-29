package clickstream.eventvisualiser.ui.internal.data.repository.fakes

import clickstream.eventvisualiser.ui.internal.data.datasource.CSEvDatasource
import clickstream.eventvisualiser.ui.internal.data.model.CSEvEvent
import clickstream.eventvisualiser.ui.internal.data.model.CSEvState
import clickstream.eventvisualiser.CSEVEventObserver
import clickstream.listener.CSEventModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class FakeEvDatasource(private val evObserver: CSEVEventObserver) : CSEvDatasource {

    private val eventDataMap = ConcurrentHashMap<String, CopyOnWriteArrayList<CSEvEvent>>()
    private var coroutineScope = CoroutineScope(SupervisorJob())
    private val connectionFlowInternal = MutableStateFlow(false)
    private val eventCallback: (List<CSEventModel>) -> Unit = {
        coroutineScope.launch { addInterceptedEvent(it) }
    }

    override fun stopObserving() {
        coroutineScope.cancel()
        evObserver.removeObserver(eventCallback)
    }

    override val isConnected: Flow<Boolean>
        get() = connectionFlowInternal.asStateFlow()

    override fun startObserving() {
        coroutineScope = CoroutineScope(SupervisorJob())
        evObserver.addObserver(eventCallback)
    }

    override suspend fun addEvents(eventList: List<CSEvEvent>) {

    }

    override suspend fun getAllEventNames(
        keys: List<String>,
        values: List<String>
    ): List<String> {
        return when {
            keys.isEmpty() && values.isEmpty() -> eventDataMap.toList().sortedByDescending {
                it.second.last().timeStampInMillis
            }.map {
                it.first
            }
            else -> filterEventsOnKeyAndValues(keys, values)
        }
    }

    override suspend fun getEventByNameAndId(eventId: String, eventName: String): CSEvEvent? {
        return eventDataMap[eventName]?.find { eventId == it.eventId }
    }

    override suspend fun getEventDetailList(eventName: String): List<CSEvEvent> {
        return (eventDataMap[eventName] ?: listOf()).asReversed()
    }

    override suspend fun getEventProperties(
        eventName: String,
        eventId: String
    ): Map<String, Any?> {
        return eventDataMap[eventName]?.find { eventId == it.eventId }?.properties?.filter {
            it.value.toString().isNotEmpty()
        } ?: mapOf()
    }

    override suspend fun clearData() {
        eventDataMap.clear()
    }

    private suspend fun addInterceptedEvent(eventList: List<CSEventModel>) =
        withContext(Dispatchers.Default) {
            eventList.forEach {
                when (it) {
                    is CSEventModel.Event.Instant -> addNewValueInCurrentList(
                        CSEvEvent(
                            eventName = it.eventName ?: "Empty event",
                            eventId = it.eventId,
                            properties = it.properties,
                            state = CSEvState.ACKNOWLEDGED,
                            timeStampInMillis = it.timeStamp * 1000L
                        )
                    )


                    is CSEventModel.Event.Scheduled ->
                        addNewValueInCurrentList(
                            CSEvEvent(
                                eventName = it.eventName ?: "Empty event",
                                eventId = it.eventId,
                                properties = it.properties,
                                state = CSEvState.SCHEDULED,
                                timeStampInMillis = it.timeStamp * 1000L
                            )
                        )

                    is CSEventModel.Event.Acknowledged -> changeEventStatusInCurrentList(
                        it,
                        CSEvState.ACKNOWLEDGED
                    )

                    is CSEventModel.Event.Dispatched -> changeEventStatusInCurrentList(
                        it,
                        CSEvState.DISPATCHED
                    )
                }
            }
        }


    private fun addNewValueInCurrentList(csEvent: CSEvEvent) {
        val eventName = csEvent.eventName
        var list = eventDataMap[eventName]
        if (list == null) {
            list = CopyOnWriteArrayList()
            eventDataMap[eventName] = list
        }
        list.add(csEvent)
    }

    private fun changeEventStatusInCurrentList(
        csEventModel: CSEventModel.Event,
        newState: CSEvState
    ) {
        eventDataMap.forEach {
            it.value.run {
                val currentPosition = indexOfFirst {
                    csEventModel.eventId == it.eventId
                }
                if (currentPosition != -1) {
                    set(currentPosition, get(currentPosition).copy(state = newState))
                }
            }
        }
    }

    private fun filterEventsOnKeyAndValues(
        keys: List<String>,
        values: List<String>
    ): List<String> {
        val eventList = mutableListOf<String>()
        val sanitizedKeys = keys.map { sanitizeStringForFilter(it) }
        val sanitizedValues = values.map { sanitizeStringForFilter(it) }
        eventDataMap.forEach {
            if (isEntryValid(it.key, sanitizedKeys, sanitizedValues)) {
                eventList.add(it.key)
            }
        }
        return eventList
    }

    private fun isEntryValid(entry: String, keys: List<String>, values: List<String>): Boolean {
        val value = eventDataMap[entry]
        val eventProps = value?.firstOrNull()?.properties ?: mapOf()
        for ((propKey, propValue) in eventProps) {
            val sanitizedKey = sanitizeStringForFilter(propKey)
            val sanitizedValue = sanitizeStringForFilter(propValue.toString())
            if (keys.find { sanitizedKey.contains(it) } != null || values.find {
                    sanitizedValue.contains(it)
                } != null) {
                return true
            }
        }
        return false
    }

    private fun sanitizeStringForFilter(string: String) = string.toLowerCase(Locale.ENGLISH).trim()
}