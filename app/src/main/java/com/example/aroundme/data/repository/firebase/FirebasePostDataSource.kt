package com.example.aroundme.data.repository.firebase

import android.net.Uri
import com.example.aroundme.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getPosts(): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .addSnapshotListener { snapshot, _ ->
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun uploadPost(post: Post): Boolean {
        return try {
            firestore.collection("posts").document(post.id).set(post).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete().await()
    }

    suspend fun uploadImage(uri: Uri, postId: String): String? {
        return try {
            val ref = FirebaseStorage.getInstance().reference
                .child("post_images/$postId.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}

