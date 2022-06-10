package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import clickstream.eventvisualiser.ui.databinding.FragmentCsEvListBinding
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CSEvEventListFragment : CSEvBaseListFragment<FragmentCsEvListBinding>() {

    private val args: CSEvEventListFragmentArgs by navArgs()

    override val bindingProvider: (LayoutInflater) -> FragmentCsEvListBinding by lazy {
        { li ->
            FragmentCsEvListBinding.inflate(li)
        }
    }
    override val recyclerViewListFlow: Flow<List<CSEvListItem>> by lazy {
        viewModel.eventListFragmentUiStateFlow.map { it.events }
    }

    override val recyclerViewProvider: (FragmentCsEvListBinding) -> RecyclerView by lazy {
        {
            it.rvEventList
        }
    }

    override val onItemClick: (CSEvListItem) -> Unit = {
        navigate(
            CSEvEventListFragmentDirections.actionCSEventListFragmentToCSEventDetailFragment(
                viewModel.eventListFragmentUiStateFlow.value.header,
                it.eventId ?: ""
            )
        )
    }

    override val headerViewProvider: (FragmentCsEvListBinding) -> TextView
        get() = {
            it.tvCsHeader
        }

    override val headerFlow: Flow<String>
        get() = viewModel.eventListFragmentUiStateFlow.map { it.header }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventName = args.eventName
        viewModel.getEventDetailsList(eventName)
    }

    override fun onDestroy() {
        viewModel.clearEventList()
        super.onDestroy()
    }

}