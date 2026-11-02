package com.zpr.rssreader.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.FragmentFeedListBinding

/**
 * 订阅源列表页：展示未读数，支持进入文章、长按删除、右下角添加。
 */
class FeedListFragment : Fragment(R.layout.fragment_feed_list) {

    private var binding: FragmentFeedListBinding? = null
    private val viewModel: FeedListViewModel by viewModels()
    private lateinit var adapter: FeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentFeedListBinding.bind(view)
        binding = currentBinding

        adapter = FeedAdapter(
            onFeedClick = { feed ->
                findNavController().navigate(
                    R.id.action_feedList_to_itemList,
                    bundleOf(
                        "feedId" to feed.id,
                        "feedTitle" to feed.title
                    )
                )
            },
            onFeedLongClick = { feed ->
                confirmDelete(feed.id, feed.title)
            }
        )
        currentBinding.feedRecyclerView.adapter = adapter

        currentBinding.addFeedButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedList_to_addFeed)
        }

        viewModel.feeds.observe(viewLifecycleOwner) { feeds ->
            adapter.submitList(feeds)
            currentBinding.emptyView.isVisible = feeds.isEmpty()
            currentBinding.feedRecyclerView.isVisible = feeds.isNotEmpty()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }

    private fun confirmDelete(feedId: Long, title: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.feed_delete_title)
            .setMessage(getString(R.string.feed_delete_message, title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteFeed(feedId)
            }
            .show()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
