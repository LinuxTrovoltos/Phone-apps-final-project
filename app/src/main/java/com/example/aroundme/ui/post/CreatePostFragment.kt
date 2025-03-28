package com.example.aroundme.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.aroundme.R
import com.example.aroundme.databinding.FragmentCreatePostBinding
import com.example.aroundme.models.Post
import com.example.aroundme.viewmodels.PostViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivSelectedImage.setImageURI(it)
            binding.ivSelectedImage.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCreatePostBinding.bind(view)

        binding.btnSelectImage.setOnClickListener { imagePicker.launch("image/*") }

        binding.etLocation.setOnClickListener { launchAddressAutocomplete() }

        binding.btnSubmit.setOnClickListener { submitPost() }
    }

    private fun launchAddressAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    selectedLatLng = place.latLng
                    selectedAddress = place.address
                    binding.etLocation.setText(selectedAddress)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(requireContext(), status.statusMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun submitPost() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val latLng = selectedLatLng

        if (title.isEmpty() || description.isEmpty() || latLng == null) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val post = Post(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            imageUrl = null,
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            creatorId = viewModel.currentUserId,
            timestamp = System.currentTimeMillis(),
            category = selectedCategoryOrDefault()
        )

        binding.btnSubmit.isEnabled = false

        viewModel.createPost(post, selectedImageUri) { success ->
            binding.btnSubmit.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Post submitted!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Post failed to upload", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectedCategoryOrDefault(): String {
        val chipId = binding.chipGroupCategories.checkedChipId
        val chip = binding.chipGroupCategories.findViewById<Chip>(chipId)
        return chip?.text?.toString() ?: "general"
    }




    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 101
    }
}
