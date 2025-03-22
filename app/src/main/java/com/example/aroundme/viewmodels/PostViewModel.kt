package com.example.aroundme.viewmodels

import android.net.Uri
import androidx.lifecycle.*
import com.example.aroundme.models.Post
import com.example.aroundme.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _filterByUserId = MutableStateFlow<String?>(null)

    val filteredPosts: LiveData<List<Post>> = combine(
        repository.getPosts(),
        _selectedCategory,
        _filterByUserId
    ) { posts, category, userId ->
        posts.filter { post ->
            (category == "All" || post.category == category) &&
                    (userId == null || post.creatorId == userId)
        }
    }.asLiveData()

    fun createPost(post: Post, imageUri: Uri?) {
        viewModelScope.launch {
            repository.createPost(post, imageUri)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setUserFilter(userId: String?) {
        _filterByUserId.value = userId
    }
}
