package com.example.aroundme.ui.post

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aroundme.R
import com.example.aroundme.adapters.PostAdapter
import com.example.aroundme.databinding.FragmentPostListBinding
import com.example.aroundme.models.Post
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostListFragment : Fragment(R.layout.fragment_post_list) {

    private var _binding: FragmentPostListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostListBinding.bind(view)

        adapter = PostAdapter { post -> navigateToDetail(post) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Fake sample data
        adapter.submitList(listOf(Post("123", "Title", "Desc", null, 0.0, 0.0, "user", 0L, null)))
    }

    private fun navigateToDetail(post: Post) {
        val action = PostListFragmentDirections
            .actionPostListFragmentToPostDetailFragment(post.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
