package clickstream.eventvisualiser.ui.internal.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import clickstream.eventvisualiser.ui.databinding.ItemCsEvLayoutBinding
import clickstream.eventvisualiser.ui.internal.ui.CSEvListItem
import clickstream.eventvisualiser.ui.internal.ui.setTextForNonNullContent

internal class CSEvViewHolder(
    private val binding: ItemCsEvLayoutBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: CSEvListItem, onClick: (CSEvListItem) -> Unit) {
        binding.tvTitle.text = item.title
        binding.tvSubtitle.setTextForNonNullContent(item.subTitle)
        binding.root.setOnClickListener {
            onClick(item)
        }

    }
}