package com.example.aroundme.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aroundme.models.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor() : ViewModel() {

    fun getPostById(postId: String): LiveData<Post?> {
        // Simulated post loading for demo
        val post = Post(postId, "Loaded Title", "Loaded Desc", null, 0.0, 0.0, "user", 0L, null)
        return MutableLiveData(post)
    }
}
