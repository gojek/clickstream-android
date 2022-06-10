package clickstream.eventvisualiser.ui.internal.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import clickstream.eventvisualiser.ui.databinding.ItemCsEvLayoutBinding
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem
import clickstream.eventvisualiser.ui.internal.ui.viewholder.CSEvViewHolder

internal class CSEvRecyclerViewAdapter :
    ListAdapter<CSEvListItem, CSEvViewHolder>(CSEvDiffUtilCallback) {

    var onClick: (CSEvListItem) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CSEvViewHolder {
        return CSEvViewHolder(
            ItemCsEvLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CSEvViewHolder, position: Int) {
        holder.onBind(currentList[position], onClick)
    }

    override fun getItemCount(): Int = currentList.size

}
