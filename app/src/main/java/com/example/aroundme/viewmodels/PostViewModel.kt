package com.example.aroundme.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.aroundme.models.Post
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.data.repository.PostRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepositoryImpl
) : ViewModel() {

    private val _allPosts = MutableStateFlow<List<PostWithUser>>(emptyList())
    private val _categoryFilter = MutableStateFlow("All")
    private val _filterMyPosts = MutableStateFlow(false)

    val postsWithUser: LiveData<List<PostWithUser>> = combine(
        _allPosts, _categoryFilter, _filterMyPosts
    ) { posts, category, onlyMine ->
        posts.filter { postWithUser ->
            val matchesUser = !onlyMine || postWithUser.post.creatorId == currentUserId
            val matchesCategory = category == "All" || postWithUser.post.category.equals(category, ignoreCase = true)
            matchesUser && matchesCategory
        }
    }.asLiveData()


    init {
        viewModelScope.launch {
            postRepository.syncPosts()
            postRepository.getPostsWithUser().collect {
                _allPosts.value = it
            }
        }
    }


    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun createPost(post: Post, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = postRepository.createPost(post, imageUri)
            onComplete(result.isSuccess)
        }
    }

    fun filterOnlyMyPosts(onlyMine: Boolean) {
        _filterMyPosts.value = onlyMine
    }

    val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
}
