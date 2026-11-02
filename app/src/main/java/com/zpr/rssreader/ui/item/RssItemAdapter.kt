package com.zpr.rssreader.ui.item

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zpr.rssreader.R
import com.zpr.rssreader.data.local.entity.RssItem
import com.zpr.rssreader.databinding.ItemRssItemBinding
import com.zpr.rssreader.util.DateUtils
import com.zpr.rssreader.util.TextUtils

class RssItemAdapter(
    private val onItemClick: (RssItem) -> Unit
) : ListAdapter<RssItem, RssItemAdapter.RssItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssItemViewHolder {
        val binding = ItemRssItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RssItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RssItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RssItemViewHolder(
        private val binding: ItemRssItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RssItem) {
            binding.itemTitleText.text = item.title.ifBlank {
                binding.root.context.getString(R.string.item_untitled)
            }
            binding.itemTitleText.setTypeface(
                null,
                if (item.isRead) Typeface.NORMAL else Typeface.BOLD
            )

            val preview = TextUtils.htmlToPlainText(item.summary ?: item.content)
            binding.itemSummaryText.isVisible = preview.isNotBlank()
            binding.itemSummaryText.text = preview

            val date = DateUtils.formatPubDate(item.pubDate)
            binding.itemDateText.isVisible = date.isNotBlank()
            binding.itemDateText.text = date

            binding.unreadDot.isVisible = !item.isRead
            binding.favoriteIcon.isVisible = item.isFavorite

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RssItem>() {

        override fun areItemsTheSame(oldItem: RssItem, newItem: RssItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RssItem, newItem: RssItem): Boolean =
            oldItem == newItem
    }
}
