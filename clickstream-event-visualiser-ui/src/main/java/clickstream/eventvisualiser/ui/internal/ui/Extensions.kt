package clickstream.eventvisualiser.ui.internal.ui

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.coroutines.flow.MutableStateFlow


internal fun TextView.setTextForNonNullContent(textContent: String?) {
    if (!textContent.isNullOrEmpty()) {
        isGone = false
        text = textContent
    } else {
        isGone = true
    }
}

internal fun <T> MutableStateFlow<T>.update(callable: (T) -> T) {
    value = callable(value)
}

internal fun Context.getColorByRes(@ColorRes colorId: Int) = ContextCompat.getColor(this, colorId)