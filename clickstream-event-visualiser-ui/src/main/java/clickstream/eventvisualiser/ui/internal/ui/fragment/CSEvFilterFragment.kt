package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import clickstream.eventvisualiser.ui.R
import clickstream.eventvisualiser.ui.databinding.FragmentCsEvFilterBinding
import clickstream.eventvisualiser.ui.databinding.ItemCsEvFilterBinding
import clickstream.eventvisualiser.ui.internal.ui.viewmodel.CSEvViewModel

internal class CSEvFilterFragment : Fragment() {

    private var binding: FragmentCsEvFilterBinding? = null
    private val viewModel by activityViewModels<CSEvViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCsEvFilterBinding.inflate(inflater).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAddButton()
        setUpApplyButton()
    }

    private fun setUpApplyButton() {
        binding?.btnCsEvDone?.setOnClickListener {
            updateEventListAndCloseFragment()
        }
    }

    private fun updateEventListAndCloseFragment() {
        val keyList = mutableListOf<String>()
        val valueList = mutableListOf<String>()
        binding?.llCsEvFilterContainer?.run {
            forEach {
                val keyEt = it.findViewById<EditText>(R.id.et_cs_ev_key).text.toString().trim()
                val valueEt = it.findViewById<EditText>(R.id.et_cs_ev_value).text.toString().trim()
                if (keyEt.isNotEmpty()) {
                    keyList.add(keyEt)
                }
                if (valueEt.isNotEmpty()) {
                    valueList.add(valueEt)
                }
            }
        }
        viewModel.getAllEventNames(keyList, valueList)
        requireActivity().onBackPressed()
    }

    private fun setUpAddButton() {
        binding?.btnCsEvAdd?.setOnClickListener {
            addNewFilterView()
        }
    }

    private fun addNewFilterView() {
        val filterView = ItemCsEvFilterBinding.inflate(requireActivity().layoutInflater).root
        binding?.llCsEvFilterContainer?.addView(filterView)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}