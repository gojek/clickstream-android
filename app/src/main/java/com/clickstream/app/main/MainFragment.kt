package com.clickstream.app.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import clickstream.eventvisualiser.ui.CSEventVisualiserUI
import com.clickstream.app.databinding.FragmentMainBinding
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.InputIntent.AgeInputIntent.Companion.setupAgeInputFlow
import com.clickstream.app.main.MainIntent.InputIntent.EmailInputIntent.Companion.setupEmailInputFlow
import com.clickstream.app.main.MainIntent.InputIntent.GenderInputIntent.Companion.setupGenderInputFlow
import com.clickstream.app.main.MainIntent.InputIntent.NameInputIntent.Companion.setupNameInputFlow
import com.clickstream.app.main.MainIntent.InputIntent.PhoneInputIntent.Companion.setupPhoneInputFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.view.clicks

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class MainFragment : Fragment() {

    private val vm: MainViewModel by viewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerObserver()
        vm.processIntents(flows())
        vm.processIntents(binding.sendEvent.clicks().map { MainIntent.SendIntent })
        setUpEventVisualiser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpEventVisualiser() {
        binding.showEv.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isGone = false
                setOnClickListener {
                    CSEventVisualiserUI.getInstance().show()
                }
            } else {
                isGone = false
            }
        }
    }

    private fun registerObserver() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(STARTED) {
                vm.states
            }
        }
    }

    private fun flows(): Flow<MainIntent> {
        return combine(
            setupNameInputFlow(binding),
            setupAgeInputFlow(binding),
            setupGenderInputFlow(binding),
            setupPhoneInputFlow(binding),
            setupEmailInputFlow(binding)
        ) { name, age, gender, phone, email ->
            InputIntent(name.value, age.value, gender.value, phone.value, email.value)
        }
    }
}
