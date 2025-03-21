package com.example.aroundme.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundme.R
import com.example.aroundme.adapters.PostAdapter
import com.example.aroundme.data.repository.PostRepositoryImpl
import com.example.aroundme.database.PostDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var repository: PostRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(emptyList())
        recyclerView.adapter = postAdapter

        val dao = PostDatabase.getDatabase(this).postDao()
        val firestore = FirebaseFirestore.getInstance()
        repository = PostRepositoryImpl(dao, firestore)

        observePosts()
    }

    private fun observePosts() {
        lifecycleScope.launch {
            repository.getAllPosts().collectLatest { posts ->
                postAdapter.updateData(posts)

                if (::mMap.isInitialized) {
                    mMap.clear()
                    posts.forEach { post ->
                        val location = LatLng(post.latitude, post.longitude)
                        mMap.addMarker(MarkerOptions().position(location).title(post.title))
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 10f))
    }
}
