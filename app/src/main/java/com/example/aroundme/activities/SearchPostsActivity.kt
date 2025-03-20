package com.example.aroundme.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundme.R
import com.example.aroundme.adapters.PostAdapter
import com.example.aroundme.database.PostDatabase
import com.example.aroundme.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchPostsActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var postDatabase: PostDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_posts)

        searchInput = findViewById(R.id.search_input)
        categorySpinner = findViewById(R.id.category_spinner)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        recyclerView.adapter = postAdapter

        postDatabase = PostDatabase.getDatabase(this)

        findViewById<Button>(R.id.search_button).setOnClickListener {
            val queryText = searchInput.text.toString()
            val selectedCategory = categorySpinner.selectedItem.toString()
            searchPosts(queryText, selectedCategory)
        }
    }

    private fun searchPosts(query: String, category: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val filteredPosts = if (query.isEmpty() && category == "All") {
                postDatabase.postDao().getAllPosts()
            } else {
                postDatabase.postDao().searchPosts(query, category)
            }

            runOnUiThread {
                postList.clear()
                postList.addAll(filteredPosts)
                postAdapter.notifyDataSetChanged()
            }
        }
    }
}
