package com.zpr.rssreader.ui.item

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.FragmentItemListBinding
import com.zpr.rssreader.ui.MainActivity

/**
 * 文章列表页：支持全部文章与指定订阅源，提供刷新与全部标为已读。
 */
class ItemListFragment : Fragment(R.layout.fragment_item_list) {

    private var binding: FragmentItemListBinding? = null
    private val viewModel: ItemListViewModel by viewModels()
    private lateinit var adapter: RssItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentItemListBinding.bind(view)
        binding = currentBinding

        val feedId = arguments?.getLong("feedId", -1L) ?: -1L
        val feedTitle = arguments?.getString("feedTitle").orEmpty()
        if (feedTitle.isNotBlank()) {
            (requireActivity() as? MainActivity)?.setToolbarTitle(feedTitle)
        }
        viewModel.setFeedId(feedId.takeIf { it > 0L })

        adapter = RssItemAdapter { item ->
            findNavController().navigate(
                R.id.action_itemList_to_itemDetail,
                bundleOf("itemId" to item.id)
            )
        }
        currentBinding.itemRecyclerView.adapter = adapter
        currentBinding.emptyView.text = getString(
            if (feedId > 0L) R.string.item_list_empty_feed else R.string.item_list_empty
        )

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            currentBinding.emptyView.isVisible = items.isEmpty()
            currentBinding.itemRecyclerView.isVisible = items.isNotEmpty()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.item_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_refresh -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.refreshing,
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.refresh()
                        true
                    }

                    R.id.action_mark_all_read -> {
                        viewModel.markAllRead()
                        true
                    }

                    else -> false
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        val feedTitle = arguments?.getString("feedTitle").orEmpty()
        if (feedTitle.isNotBlank()) {
            (requireActivity() as? MainActivity)?.setToolbarTitle(feedTitle)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
