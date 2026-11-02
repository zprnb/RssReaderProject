package com.zpr.rssreader.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zpr.rssreader.R
import com.zpr.rssreader.data.local.entity.RssFeedWithUnread
import com.zpr.rssreader.databinding.ItemFeedBinding

/**
 * 订阅源列表适配器，展示标题、地址与未读数，支持点击和长按。
 */
class FeedAdapter(
    private val onFeedClick: (RssFeedWithUnread) -> Unit,
    private val onFeedLongClick: (RssFeedWithUnread) -> Unit = {}
) : ListAdapter<RssFeedWithUnread, FeedAdapter.FeedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeedViewHolder(
        private val binding: ItemFeedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feed: RssFeedWithUnread) {
            binding.feedTitleText.text = feed.title
            binding.feedUrlText.text = feed.siteUrl ?: feed.url
            binding.unreadCountText.isVisible = feed.unreadCount > 0
            binding.unreadCountText.text = feed.unreadCount.toString()
            binding.unreadCountText.contentDescription = binding.root.context.getString(
                R.string.feed_unread_content_description,
                feed.unreadCount
            )
            binding.root.setOnClickListener { onFeedClick(feed) }
            binding.root.setOnLongClickListener {
                onFeedLongClick(feed)
                true
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RssFeedWithUnread>() {

        override fun areItemsTheSame(
            oldItem: RssFeedWithUnread,
            newItem: RssFeedWithUnread
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: RssFeedWithUnread,
            newItem: RssFeedWithUnread
        ): Boolean = oldItem == newItem
    }
}
