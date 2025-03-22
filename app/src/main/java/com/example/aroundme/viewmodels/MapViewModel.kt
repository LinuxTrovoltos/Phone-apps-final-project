package com.example.aroundme.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val postsWithUser: LiveData<List<PostWithUser>> = combine(
        repository.getPostsWithUser(),
        _selectedCategory
    ) { posts, selected ->
        posts.filter { postWithUser ->
            selected == null || postWithUser.post.category.equals(selected, ignoreCase = true)
        }
    }.asLiveData()

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }
}



