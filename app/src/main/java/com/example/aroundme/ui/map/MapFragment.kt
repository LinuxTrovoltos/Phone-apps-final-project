package com.example.aroundme.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.aroundme.R
import com.example.aroundme.databinding.FragmentMapBinding
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentMapBinding.bind(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map)
                as? SupportMapFragment

        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            setupInfoWindowAdapter()
            observePosts()
        }
        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_createPostFragment)
        }
    }

    private fun observePosts() {
        viewModel.postsWithUser.observe(viewLifecycleOwner) { posts ->
            map.clear()
            posts.forEach { item ->
                val latLng = LatLng(item.post.latitude, item.post.longitude)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(item.post.title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                marker?.tag = item
            }

            if (posts.isNotEmpty()) {
                val first = posts.first().post
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 12f))
            }
        }
    }

    private fun setupInfoWindowAdapter() {
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View? {
                val item = marker.tag as? PostWithUser ?: return null
                val view = layoutInflater.inflate(R.layout.custom_info_balloon, null)

                view.findViewById<TextView>(R.id.tvUsername).text = item.user?.username ?: "Unknown"
                view.findViewById<TextView>(R.id.tvTitle).text = item.post.title
                view.findViewById<TextView>(R.id.tvDescription).text = item.post.description

                Glide.with(requireContext())
                    .load(item.user?.profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(view.findViewById(R.id.ivProfile))

                val ivPostImage = view.findViewById<ImageView>(R.id.ivPostImage)
                if (!item.post.imageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(item.post.imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(ivPostImage)
                } else {
                    ivPostImage.visibility = View.GONE
                }

                return view
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
