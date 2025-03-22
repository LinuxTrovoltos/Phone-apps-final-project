package com.example.aroundme.ui.profile

import android.app.Application
import androidx.lifecycle.*
import com.example.aroundme.database.UserDatabase
import com.example.aroundme.database.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class UserProfile(
    val username: String = "",
    val profilePhotoUrl: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userDao = UserDatabase.getInstance(application).userDao()

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // 1. Load from local DB
            val cachedUser = userDao.getUser(uid)
            if (cachedUser != null) {
                _userProfile.value = UserProfile(
                    cachedUser.username,
                    cachedUser.profilePhotoUrl
                )
            }

            // 2. Always fetch fresh from Firestore and update local DB
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val username = doc.getString("username") ?: ""
                    val photoUrl = doc.getString("profilePhoto")

                    _userProfile.value = UserProfile(username, photoUrl)

                    // Save to Room
                    viewModelScope.launch {
                        userDao.insertUser(
                            UserEntity(
                                uid = uid,
                                email = doc.getString("email") ?: "",
                                username = username,
                                profilePhotoUrl = photoUrl
                            )
                        )
                    }
                }
        }
    }
}
