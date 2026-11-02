package com.zpr.rssreader.ui.feed

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.FragmentAddFeedBinding

/**
 * 添加订阅源页面：校验输入、调用同步仓库并展示加载与错误状态。
 */
class AddFeedFragment : Fragment(R.layout.fragment_add_feed) {

    private var binding: FragmentAddFeedBinding? = null
    private val viewModel: AddFeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentAddFeedBinding.bind(view)
        binding = currentBinding

        currentBinding.cancelButton.setOnClickListener {
            hideKeyboard()
            findNavController().navigateUp()
        }
        currentBinding.addButton.setOnClickListener {
            submit()
        }
        currentBinding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                currentBinding.nameEditText.requestFocus()
                true
            } else {
                false
            }
        }
        currentBinding.nameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            currentBinding.loadingIndicator.isVisible = loading
            currentBinding.addButton.isEnabled = !loading
            currentBinding.cancelButton.isEnabled = !loading
            currentBinding.addButton.text = getString(
                if (loading) R.string.add_feed_loading else R.string.action_add
            )
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                currentBinding.urlInputLayout.error = error
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.add_feed_success,
                        result.title,
                        result.itemCount
                    ),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearSuccess()
                findNavController().navigateUp()
            }
        }
    }

    private fun submit() {
        val currentBinding = binding ?: return
        val url = currentBinding.urlEditText.text?.toString()?.trim().orEmpty()
        val name = currentBinding.nameEditText.text?.toString()?.trim().orEmpty()

        if (url.isEmpty()) {
            currentBinding.urlInputLayout.error = getString(R.string.add_feed_invalid_url)
            return
        }

        currentBinding.urlInputLayout.error = null
        hideKeyboard()
        viewModel.addFeed(url, name.takeIf { it.isNotEmpty() })
    }

    private fun hideKeyboard() {
        val manager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(binding?.root?.windowToken, 0)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
