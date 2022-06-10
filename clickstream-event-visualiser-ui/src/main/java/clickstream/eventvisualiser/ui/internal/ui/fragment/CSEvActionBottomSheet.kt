package clickstream.eventvisualiser.ui.internal.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import clickstream.eventvisualiser.ui.databinding.FragmentCsEvActionBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*

internal class CSEvActionBottomSheet(context: Context) : BottomSheetDialog(context) {

    private var coroutineScope: CoroutineScope? = CoroutineScope(SupervisorJob())

    private var binding: FragmentCsEvActionBottomsheetBinding =
        FragmentCsEvActionBottomsheetBinding.inflate(
            LayoutInflater.from(context)
        )

    init {
        setContentView(binding.root)
    }

    var actionListener: CSEvActionCallbacks? = null
        set(value) {
            field = value
            field?.run {
                setUpClicks(binding.tvCsEvStartCapture, ::startCapture)
                setUpClicks(binding.tvCsEvStopCapture, ::stopCapture)
                setUpClicks(binding.tvCsEvClearData, ::clearData)
                setUpClicks(binding.tvCsEvClose, ::close)
            }
        }

    private fun setUpClicks(textView: TextView?, onClickAction: suspend () -> Unit) {
        textView?.setOnClickListener {
            coroutineScope?.launch {
                onClickAction()
                dismiss()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        coroutineScope = CoroutineScope(SupervisorJob())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel()
        coroutineScope = null

    }

    interface CSEvActionCallbacks {
        suspend fun startCapture()
        suspend fun stopCapture()
        suspend fun clearData()
        suspend fun close()
    }
}

