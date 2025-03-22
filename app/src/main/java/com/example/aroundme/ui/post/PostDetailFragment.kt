package com.example.aroundme.ui.post

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.aroundme.R
import com.example.aroundme.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private val args: PostDetailFragmentArgs by navArgs()
    private val viewModel: PostViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val postId = args.postId

        val titleView: TextView = view.findViewById(R.id.postTitleTextView)

        // Load post by ID (demo only)
        viewModel.getPostById(postId).observe(viewLifecycleOwner) { post ->
            titleView.text = post?.title ?: "Post not found"
        }
    }
}
