package clickstream.eventvisualiser.ui.internal.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem

internal object CSEvDiffUtilCallback : DiffUtil.ItemCallback<CSEvListItem>() {
    override fun areItemsTheSame(oldItem: CSEvListItem, newItem: CSEvListItem): Boolean {
        return oldItem.eventId == newItem.eventId
    }

    override fun areContentsTheSame(oldItem: CSEvListItem, newItem: CSEvListItem): Boolean {
        return oldItem == newItem
    }
}