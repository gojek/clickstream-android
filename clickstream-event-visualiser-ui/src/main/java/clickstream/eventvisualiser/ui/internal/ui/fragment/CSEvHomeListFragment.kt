package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import clickstream.eventvisualiser.ui.R
import clickstream.eventvisualiser.ui.databinding.FragmentCsEvHomeBinding
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class CSEvHomeListFragment : CSEvBaseListFragment<FragmentCsEvHomeBinding>() {

    override val bindingProvider by lazy {
        { li: LayoutInflater ->
            FragmentCsEvHomeBinding.inflate(li)
        }
    }
    override val recyclerViewListFlow by lazy {
        viewModel.homeFragmentUiStateFlow.map { it.events }
    }

    override val recyclerViewProvider by lazy {
        { binding: FragmentCsEvHomeBinding ->
            binding.rvCsHome
        }
    }

    override val onItemClick: (CSEvListItem) -> Unit = ::goToEventsListScreen


    override val headerFlow by lazy {
        viewModel.homeFragmentUiStateFlow.map { it.header }
    }

    override val headerViewProvider by lazy {
        { binding: FragmentCsEvHomeBinding ->
            binding.tvHomeHeader
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSearch()
        setUpFilter()
        observeConnection()
    }

    private fun observeConnection() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.homeFragmentUiStateFlow.map { it.isConnected }.collect { isConnected ->
                val connectionColorDrawableRes =
                    if (isConnected) R.drawable.ic_baseline_wifi_24 else R.drawable.ic_baseline_wifi_off_24
                binding?.ivConnection?.setImageResource(connectionColorDrawableRes)
            }
        }
    }

    private fun setUpFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.homeFragmentUiStateFlow.map { it.isFilterApplied }.collect {
                binding?.tvHomeFilter?.text = if (it) "Remove filter" else "Add filter"
            }
        }
        binding?.tvHomeFilter?.setOnClickListener {
            val isFilterApplied = viewModel.homeFragmentUiStateFlow.value.isFilterApplied
            if (isFilterApplied) {
                viewModel.getAllEventNames()
            } else {
                navigate(CSEvHomeListFragmentDirections.actionCSHomeFragmentToCSEvFilterFragment())
            }
        }
    }

    private fun goToEventsListScreen(item: CSEvListItem) {
        navigate(CSEvHomeListFragmentDirections.actionCSHomeFragmentToCSEventListFragment(item.title))
    }

    private fun setUpSearch() {
        binding?.etCsSearch?.doOnTextChanged { newText, _, _, _ ->
            viewModel.filterEventsOnKeyword(newText.toString())
        }
    }

}