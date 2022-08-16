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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

internal class CSEvViewModel constructor(private val csRepo: CSEvRepository) :
    ViewModel() {

    private val _homeFragmentUiStateFlow = MutableStateFlow(CSEvHomeUIState())
    val homeFragmentUiStateFlow = _homeFragmentUiStateFlow.asStateFlow()

    private val _eventListFragmentUiStateFlow = MutableStateFlow(CSEvUIState())
    val eventListFragmentUiStateFlow = _eventListFragmentUiStateFlow.asStateFlow()

    private val _eventDetailFragmentUiStateFlow = MutableStateFlow(CSEvDetailUIState())
    val eventDetailFragmentUiStateFlow = _eventDetailFragmentUiStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            csRepo.isConnected.collect { isConnected ->
                _homeFragmentUiStateFlow.update {
                    it.copy(isConnected = isConnected)
                }
            }
        }
    }

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
                CSEvDetailUIState(
                    originalList = eventProperties,
                    events = eventProperties,
                    header = "$eventName details",
                )
            }
        }

    fun filterEventsOnKeyword(text: String) = viewModelScope.launch(Dispatchers.Default) {
        _homeFragmentUiStateFlow.run {
            val events = filter(value, text) {
                val properKey = text.trim().toLowerCase()
                it.title.toLowerCase().contains(properKey)

            }
            update { it.copy(events = events) }
        }
    }

    fun filterPropertiesOnKeyword(keyword: String) = viewModelScope.launch(Dispatchers.Default) {
        _eventDetailFragmentUiStateFlow.run {
            val events = filter(value, keyword) {
                val properKey = keyword.trim().toLowerCase()
                it.title.toLowerCase().contains(properKey) or (it.subTitle?.toLowerCase()
                    ?.contains(properKey) ?: false)
            }
            update { it.copy(events = events) }
        }
    }


    private fun filter(
        uiState: CSEvUIState,
        keyword: String,
        filterLogic: (CSEvListItem) -> Boolean
    ): List<CSEvListItem> {
        return if (keyword.isEmpty()) {
            uiState.originalList
        } else {
            uiState.originalList.filter(filterLogic)
        }
    }

    fun clearEventDetailList() {
        _eventDetailFragmentUiStateFlow.update {
            CSEvDetailUIState(
                events = listOf(),
                header = it.header,
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