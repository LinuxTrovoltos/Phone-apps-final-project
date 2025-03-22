package com.example.aroundme.ui.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.aroundme.R
import com.example.aroundme.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            map.clear()
            posts.forEach {
                val marker = MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.title)
                map.addMarker(marker)
            }
            if (posts.isNotEmpty()) {
                val firstPost = posts.first()
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPost.latitude, firstPost.longitude), 12f))
            }
        }
    }
}
