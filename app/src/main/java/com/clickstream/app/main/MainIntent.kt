package com.clickstream.app.main

import com.clickstream.app.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import reactivecircus.flowbinding.android.widget.afterTextChanges

interface MainIntentValue {
    val value: String?
}

sealed class MainIntent : MainIntentValue {

    override val value: String?
        get() = null

    open class InputIntent(
        val name: String?,
        val age: String?,
        val gender: String?,
        val phone: String?,
        val email: String?
    ) : MainIntent() {

        constructor() : this(null, null, null, null, null)

        data class NameInputIntent(override val value: String?) : InputIntent() {
            companion object {
                fun setupNameInputFlow(binding: FragmentMainBinding): Flow<NameInputIntent> {
                    return binding.name.editText
                        ?.afterTextChanges()
                        ?.map { NameInputIntent(it.editable.toString()) }
                        ?: emptyFlow()
                }
            }
        }

        data class AgeInputIntent(override val value: String?) : InputIntent() {
            companion object {
                fun setupAgeInputFlow(binding: FragmentMainBinding): Flow<AgeInputIntent> {
                    return binding.age.editText
                        ?.afterTextChanges()
                        ?.map { AgeInputIntent(it.editable.toString()) }
                        ?: emptyFlow()
                }
            }
        }

        data class GenderInputIntent(override val value: String?) : InputIntent() {
            companion object {
                fun setupGenderInputFlow(binding: FragmentMainBinding): Flow<GenderInputIntent> {
                    return binding.gender.editText
                        ?.afterTextChanges()
                        ?.map { GenderInputIntent(it.editable.toString()) }
                        ?: emptyFlow()
                }
            }
        }

        data class PhoneInputIntent(override val value: String?) : InputIntent() {
            companion object {
                fun setupPhoneInputFlow(binding: FragmentMainBinding): Flow<PhoneInputIntent> {
                    return binding.phone.editText
                        ?.afterTextChanges()
                        ?.map { PhoneInputIntent(it.editable.toString()) }
                        ?: emptyFlow()
                }
            }
        }

        data class EmailInputIntent(override val value: String?) : InputIntent() {
            companion object {
                fun setupEmailInputFlow(binding: FragmentMainBinding): Flow<EmailInputIntent> {
                    return binding.email.editText
                        ?.afterTextChanges()
                        ?.map { EmailInputIntent(it.editable.toString()) }
                        ?: emptyFlow()
                }
            }
        }
    }

    object ConnectIntent : MainIntent()

    object DisconnectIntent : MainIntent()

    object SendIntent : MainIntent()
}
