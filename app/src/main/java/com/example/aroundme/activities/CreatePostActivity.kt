package com.example.aroundme.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aroundme.R
import com.example.aroundme.database.PostDatabase
import com.example.aroundme.data.repository.PostRepositoryImpl
import com.example.aroundme.models.Post
import com.example.aroundme.utils.SyncHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var saveButton: Button
    private lateinit var repository: PostRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        titleInput = findViewById(R.id.post_title_input)
        descriptionInput = findViewById(R.id.post_description_input)
        saveButton = findViewById(R.id.save_post_button)

        val dao = PostDatabase.getDatabase(this).postDao()
        val firestore = FirebaseFirestore.getInstance()
        repository = PostRepositoryImpl(dao, firestore)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val post = Post(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    latitude = 0.0,
                    longitude = 0.0,
                    isSynced = false,
                    updatedAt = System.currentTimeMillis()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    repository.addOrUpdatePost(post)

                    if (isInternetAvailable()) {
                        SyncHelper.triggerSync(this@CreatePostActivity)
                    }

                    runOnUiThread {
                        Toast.makeText(this@CreatePostActivity, "Post saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
