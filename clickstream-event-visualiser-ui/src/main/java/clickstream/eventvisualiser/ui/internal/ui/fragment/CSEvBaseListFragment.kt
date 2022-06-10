package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem
import clickstream.eventvisualiser.ui.internal.ui.adapter.CSEvRecyclerViewAdapter
import clickstream.eventvisualiser.ui.internal.ui.viewmodel.CSEvViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

internal abstract class CSEvBaseListFragment<T : ViewBinding> : Fragment() {

    open val onItemClick: (CSEvListItem) -> Unit = {}

    abstract val bindingProvider: (LayoutInflater) -> T

    abstract val recyclerViewListFlow: Flow<List<CSEvListItem>>

    abstract val recyclerViewProvider: (T) -> RecyclerView

    abstract val headerViewProvider: (T) -> TextView

    abstract val headerFlow: Flow<String>

    protected val viewModel by activityViewModels<CSEvViewModel>()

    protected var binding: T? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return bindingProvider(inflater).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setUpHeaderView()

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected fun navigate(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun setUpRecyclerView() {
        binding?.run {
            val recyclerViewAdapter = CSEvRecyclerViewAdapter().apply {
                onClick = onItemClick
            }
            recyclerViewProvider(this).run {
                adapter = recyclerViewAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                recyclerViewListFlow.collect {
                    recyclerViewAdapter.submitList(it)
                }
            }
        }

    }

    private fun setUpHeaderView() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            headerFlow.collect {
                binding?.run { headerViewProvider.invoke(this).text = it }

            }
        }
    }
}