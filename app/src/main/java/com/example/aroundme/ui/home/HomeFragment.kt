package com.example.aroundme.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.aroundme.R
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.databinding.FragmentHomeBinding
import com.example.aroundme.viewmodels.PostViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var googleMap: GoogleMap

    private var isMapReady = false
    private var postList: List<PostWithUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        setupButtons()
        setupMap()
        setupFilters()

        viewModel.postsWithUser.observe(viewLifecycleOwner) { posts ->
            postList = posts
            maybeRenderMarkers()
        }
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToWelcomeFragment())
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
        }

        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionMapFragmentToCreatePostFragment())
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, _ ->
            val selected = getSelectedCategoryFilter()

            if (selected == "Mine") {
                viewModel.filterOnlyMyPosts(true)
            } else {
                viewModel.setCategoryFilter(selected)
                viewModel.filterOnlyMyPosts(false)
            }
        }
    }

    private fun getSelectedCategoryFilter(): String {
        val chipId = binding.chipGroupFilters.checkedChipId
        val chip = binding.chipGroupFilters.findViewById<Chip>(chipId)
        return chip?.text?.toString() ?: "All"
    }


    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.08, 34.78), 12f))
        googleMap.setInfoWindowAdapter(PostInfoWindowAdapter())

        maybeRenderMarkers()
    }

    private fun maybeRenderMarkers() {
        if (!isMapReady || postList.isEmpty()) return

        googleMap.clear()
        postList.forEach { item ->
            val position = LatLng(item.post.latitude, item.post.longitude)
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(item.post.title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            marker?.tag = item
        }
    }

    inner class PostInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)

        override fun getInfoWindow(marker: Marker): View? = null

        override fun getInfoContents(marker: Marker): View {
            val data = marker.tag as? PostWithUser ?: return window
            with(window) {
                findViewById<android.widget.TextView>(R.id.tvUsername).text = data.user?.username
                findViewById<android.widget.TextView>(R.id.tvTitle).text = data.post.title
                findViewById<android.widget.TextView>(R.id.tvDescription).text = data.post.description

                val ivProfile = findViewById<android.widget.ImageView>(R.id.ivProfile)
                Glide.with(this).load(data.user?.profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile)

                val ivPost = findViewById<android.widget.ImageView>(R.id.ivPostImage)
                if (!data.post.imageUrl.isNullOrEmpty()) {
                    ivPost.visibility = View.VISIBLE
                    Glide.with(this).load(data.post.imageUrl)
                        .placeholder(R.drawable.ic_home)
                        .into(ivPost)
                } else {
                    ivPost.visibility = View.GONE
                }
            }
            return window
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
