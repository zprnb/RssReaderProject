package com.zpr.rssreader.ui.item

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zpr.rssreader.R
import com.zpr.rssreader.data.local.entity.RssItem
import com.zpr.rssreader.databinding.FragmentItemDetailBinding
import com.zpr.rssreader.util.DateUtils

/**
 * 文章详情页：渲染正文、自动标记已读、收藏切换与浏览器打开。
 */
class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    private var binding: FragmentItemDetailBinding? = null
    private val viewModel: ItemDetailViewModel by viewModels()
    private var currentItem: RssItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentItemDetailBinding.bind(view)
        binding = currentBinding

        val itemId = arguments?.getLong("itemId", -1L) ?: -1L
        viewModel.setItemId(itemId)
        viewModel.markReadOnce()

        currentBinding.openLinkButton.setOnClickListener {
            openInBrowser()
        }

        viewModel.item.observe(viewLifecycleOwner) { item ->
            currentItem = item
            if (item == null) {
                currentBinding.detailTitleText.text = getString(R.string.item_not_found)
                currentBinding.detailMetaText.text = ""
                currentBinding.detailContentText.text = ""
                currentBinding.openLinkButton.isVisible = false
            } else {
                currentBinding.detailTitleText.text = item.title.ifBlank {
                    getString(R.string.item_untitled)
                }
                currentBinding.detailMetaText.text = buildMeta(item)
                val content = item.content ?: item.summary
                currentBinding.detailContentText.text = if (content.isNullOrBlank()) {
                    getString(R.string.item_detail_no_content)
                } else {
                    Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                }
                currentBinding.openLinkButton.isVisible = !item.link.isNullOrBlank()
            }
            requireActivity().invalidateOptionsMenu()
        }

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_menu, menu)
                menu.findItem(R.id.action_favorite).title = getString(
                    if (currentItem?.isFavorite == true) {
                        R.string.action_unfavorite
                    } else {
                        R.string.action_favorite
                    }
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_favorite -> {
                        viewModel.toggleFavorite()
                        true
                    }

                    R.id.action_open_browser -> {
                        openInBrowser()
                        true
                    }

                    else -> false
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    private fun buildMeta(item: RssItem): String {
        val date = DateUtils.formatPubDate(item.pubDate)
        val author = item.author
            ?.takeIf { it.isNotBlank() }
            ?.let { getString(R.string.item_detail_author, it) }
        return listOfNotNull(
            date.takeIf { it.isNotBlank() },
            author
        ).joinToString(" · ")
    }

    private fun openInBrowser() {
        val link = currentItem?.link
        if (link.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.open_in_browser_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(
                requireContext(),
                R.string.open_in_browser_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        binding = null
        currentItem = null
        super.onDestroyView()
    }
}
