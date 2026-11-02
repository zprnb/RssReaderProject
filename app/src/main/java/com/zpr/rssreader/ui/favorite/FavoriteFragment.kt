package com.zpr.rssreader.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.FragmentItemListBinding
import com.zpr.rssreader.ui.item.RssItemAdapter

/**
 * 收藏列表页：复用文章条目布局与适配器。
 */
class FavoriteFragment : Fragment(R.layout.fragment_item_list) {

    private var binding: FragmentItemListBinding? = null
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var adapter: RssItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentItemListBinding.bind(view)
        binding = currentBinding

        adapter = RssItemAdapter { item ->
            findNavController().navigate(
                R.id.action_favorite_to_itemDetail,
                bundleOf("itemId" to item.id)
            )
        }
        currentBinding.itemRecyclerView.adapter = adapter
        currentBinding.emptyView.text = getString(R.string.favorite_empty)

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            currentBinding.emptyView.isVisible = items.isEmpty()
            currentBinding.itemRecyclerView.isVisible = items.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
