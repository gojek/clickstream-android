package clickstream.eventvisualiser.ui.internal.ui.viewmodel

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clickstream.eventvisualiser.ui.internal.data.datasource.CSEvDatasourceImpl
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepository
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepositoryImpl
import clickstream.eventvisualiser.ui.internal.ui.*
import clickstream.eventvisualiser.ui.internal.ui.CSEvUIState
import clickstream.eventvisualiser.ui.internal.ui.CSEvHomeUIState
import clickstream.eventvisualiser.ui.internal.ui.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

internal class CSEvViewModel constructor(private val csRepo: CSEvRepository) :
    ViewModel() {

    private val _homeFragmentUiStateFlow = MutableStateFlow(CSEvHomeUIState())
    val homeFragmentUiStateFlow = _homeFragmentUiStateFlow.asStateFlow()

    private val _eventListFragmentUiStateFlow = MutableStateFlow(CSEvUIState())
    val eventListFragmentUiStateFlow = _eventListFragmentUiStateFlow.asStateFlow()

    private val _eventDetailFragmentUiStateFlow = MutableStateFlow(CSEvUIState())
    val eventDetailFragmentUiStateFlow = _eventDetailFragmentUiStateFlow.asStateFlow()

    fun getAllEventNames(
        keyList: List<String> = emptyList(),
        valueList: List<String> = emptyList()
    ) = viewModelScope.launch(Dispatchers.Default) {
        val events =
            csRepo.getAllEventNames(keyList, valueList).map { CSEvListItem(title = it) }
        _homeFragmentUiStateFlow.update {
            it.copy(
                events = events,
                originalList = events,
                isFilterApplied = keyList.isNotEmpty() || valueList.isNotEmpty()
            )
        }
    }

    fun getEventDetailsList(eventName: String) = viewModelScope.launch(Dispatchers.Default) {
        val events = csRepo.getEventDetailList(eventName)
            .map {
                CSEvListItem(
                    title = Date(it.timeStampInMillis).toString(),
                    subTitle = it.state.tag,
                    eventId = it.eventId
                )
            }
        _eventListFragmentUiStateFlow.update {
            CSEvUIState(events = events, header = eventName)
        }
    }

    fun getEventProperties(eventName: String, eventId: String) =
        viewModelScope.launch {
            val eventProperties = csRepo.getEventProperties(eventName, eventId)
                .map { CSEvListItem(title = it.key, subTitle = it.value.toString()) }
            _eventDetailFragmentUiStateFlow.update {
                CSEvUIState(events = eventProperties, header = "$eventName details")

            }
        }

    fun filterEvents(text: String) = viewModelScope.launch(Dispatchers.Default) {
        _homeFragmentUiStateFlow.run {
            val events = if (text.isEmpty()) {
                value.originalList
            } else {
                val properKey = text.trim().toLowerCase()
                value.originalList.filter {
                    it.title.toLowerCase().contains(properKey)
                }
            }
            update { it.copy(events = events) }
        }
    }

    fun clearEventDetailList() {
        _eventDetailFragmentUiStateFlow.update {
            CSEvUIState(
                events = listOf(),
                header = it.header
            )
        }
    }

    fun clearEventList() {
        _eventListFragmentUiStateFlow.update {
            CSEvUIState(
                events = listOf(),
                header = it.header
            )
        }
    }

    companion object {
        internal fun get(activity: ComponentActivity): CSEvViewModel {
            return activity.viewModels<CSEvViewModel> {
                CSEventViewModelFactory(
                    activity,
                    CSEvRepositoryImpl(CSEvDatasourceImpl.getInstance())
                )
            }.value
        }
    }
}