package com.example.aroundme.activities
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.aroundme.R
//import com.example.aroundme.database.PostDatabase
//import com.example.aroundme.models.Post
//import com.example.aroundme.utils.SyncHelper
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class CreatePostActivity : AppCompatActivity() {
//
//    private lateinit var titleInput: EditText
//    private lateinit var descriptionInput: EditText
//    private lateinit var saveButton: Button
//    private val db = FirebaseFirestore.getInstance()
//    private lateinit var postDatabase: PostDatabase
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_create_post)
//
//        titleInput = findViewById(R.id.post_title_input)
//        descriptionInput = findViewById(R.id.post_description_input)
//        saveButton = findViewById(R.id.save_post_button)
//        postDatabase = PostDatabase.getDatabase(this)
//
//        saveButton.setOnClickListener {
//            val title = titleInput.text.toString().trim()
//            val description = descriptionInput.text.toString().trim()
//
//            if (title.isNotEmpty() && description.isNotEmpty()) {
//                val post = Post(title = title, description = description, latitude = 0.0, longitude = 0.0)
//
//                if (isInternetAvailable()) {
//                    uploadPostToFirestore(post)
//                } else {
//                    cachePostLocally(post)
//                    SyncHelper.triggerSync(this) // Automatically sync when online
//                    Toast.makeText(this, "Post saved offline. Syncing when online!", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            } else {
//                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun uploadPostToFirestore(post: Post) {
//        db.collection("posts").add(post)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to upload post", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun cachePostLocally(post: Post) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            postDatabase.postDao().insertPost(post)
//        }
//    }
//
//    private fun isInternetAvailable(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }
//}
