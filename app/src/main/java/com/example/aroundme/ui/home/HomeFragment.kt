package com.example.aroundme.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.aroundme.R
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.databinding.FragmentHomeBinding
import com.example.aroundme.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleMap: GoogleMap

    private val viewModel: MapViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

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

        // 👇 Observe posts immediately, before map init
        viewModel.postsWithUser.observe(viewLifecycleOwner) { posts ->
            if (::googleMap.isInitialized) {
                updateMarkers(posts)
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            setupMap()
        }

        setupCategoryChips()
    }

    private fun setupMap() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.08, 34.78), 12f))
    }

    private fun updateMarkers(posts: List<PostWithUser>) {
        googleMap.clear()
        posts.forEach { item ->
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


    private fun setupCategoryChips() {
        val categories = listOf("All", "Food", "Event", "Lost", "Service")
        categories.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isClickable = true
                setOnClickListener {
                    viewModel.setCategoryFilter(if (label == "All") null else label)
                }
            }
            binding.chipGroupFilters.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
