package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import clickstream.eventvisualiser.ui.databinding.FragmentCsEvDetailBinding
import kotlinx.coroutines.flow.map

internal class CSEvDetailListFragment : CSEvBaseListFragment<FragmentCsEvDetailBinding>() {

    private val args: CSEvDetailListFragmentArgs by navArgs()

    override val bindingProvider by lazy {
        { li: LayoutInflater ->
            FragmentCsEvDetailBinding.inflate(li)
        }
    }

    override val recyclerViewListFlow by lazy {
        viewModel.eventDetailFragmentUiStateFlow.map { it.events }
    }

    override val recyclerViewProvider by lazy {
        { binding: FragmentCsEvDetailBinding ->
            binding.rvCsEventProperties
        }
    }

    override val headerFlow by lazy {
        viewModel.eventDetailFragmentUiStateFlow.map { it.header }
    }

    override val headerViewProvider: (FragmentCsEvDetailBinding) -> TextView
        get() = {
            it.tvCsHeader
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getEventProperties(args.eventName, args.eventId)
    }

    override fun onDestroy() {
        viewModel.clearEventDetailList()
        super.onDestroy()
    }

}