package clickstream.eventvisualiser.ui.internal.ui.views

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import clickstream.eventvisualiser.ui.databinding.LayoutCsEvWindowBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val CLICK_THRESHOLD = 5

@RequiresApi(Build.VERSION_CODES.O)
internal class CSEvFloatingWindow @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    attr: Int = 0
) : CardView(context, attributeSet, attr), View.OnTouchListener {

    private var binding: LayoutCsEvWindowBinding? = null
    private var hasMoved = false
    private val param = getWindowParams()
    private val paramsF: WindowManager.LayoutParams = param
    private var initialPoint = Point(0, 0)
    private var initialTouchPoint = Point(0, 0)

    internal var scope: CoroutineScope? = null

    internal var layoutInflater: LayoutInflater? = null
        set(value) {
            field = value
            binding = LayoutCsEvWindowBinding.inflate(field!!, this)
            invalidate()
        }

    internal var onSettingsClick: (() -> Unit)? = null
        set(value) {
            field = value
            binding?.ivSettings?.setOnClickListener {
                field?.invoke()
            }
        }

    internal var windowStateFlow: Flow<State>? = null
        set(value) {
            field = value
            scope?.launch {
                handleActionFlow(field)
            }
        }

    init {
        setCornerRadius()
        handleTouch()
    }

    internal fun addToWindow() {
        getWindowManger().addView(this, param)
    }

    private fun handleTouch() {
        setOnTouchListener(this)
    }

    private fun setCornerRadius() {
        radius = 16F
    }

    private fun closeSelf() {
        context.getSystemService<WindowManager>()?.removeView(this)
    }

    private suspend fun handleActionFlow(flow: Flow<State>?) {
        flow?.collect {
            when (it) {
                State.ACTIVE -> setCardBackgroundColor(Color.parseColor("#FF03DAC5"))
                State.DEACTIVE -> setCardBackgroundColor(Color.GRAY)
                State.HIDE -> isGone = true
                State.SHOW -> isGone = false
                State.CLOSE -> closeSelf()
            }
        }
    }

    private fun getWindowManger() =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private fun getWindowParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
    }

    override fun onTouch(p0: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                hasMoved = false
                initialPoint = Point(paramsF.x, paramsF.y)
                initialTouchPoint = Point(event.rawX.toInt(), event.rawY.toInt())
            }
            MotionEvent.ACTION_UP -> {
                return hasMoved
            }
            MotionEvent.ACTION_MOVE -> {
                val xDiff = (event.rawX - initialTouchPoint.x).toInt()
                val yDiff = (event.rawY - initialTouchPoint.y).toInt()
                hasMoved = xDiff > CLICK_THRESHOLD && yDiff > CLICK_THRESHOLD
                paramsF.x = initialPoint.x + xDiff
                paramsF.y = initialPoint.y + yDiff
                getWindowManger().updateViewLayout(this, paramsF)
                return true
            }
        }
        return false
    }

    internal enum class State {
        ACTIVE,
        DEACTIVE,
        HIDE,
        CLOSE,
        SHOW,
    }
}