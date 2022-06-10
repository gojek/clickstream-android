package clickstream.eventvisualiser.ui.internal.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepository

internal class CSEventViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: CSEvRepository,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {
        return CSEvViewModel(repository) as T
    }
}
