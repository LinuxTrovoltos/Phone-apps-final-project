package com.example.aroundme.ui.profile

import android.graphics.Bitmap
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aroundme.database.UserDatabase
import com.example.aroundme.database.UserEntity
import com.example.aroundme.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private val userDao by lazy {
        UserDatabase.getInstance(requireContext().applicationContext).userDao()
    }

    private var selectedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserInfo()

        binding.btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username")
                val profilePhoto = doc.getString("profilePhoto")

                binding.etUsername.setText(username)

                if (!profilePhoto.isNullOrEmpty()) {
                    Picasso.get().load(profilePhoto).into(binding.ivPreview)
                }
            }
    }

    private fun saveProfileChanges() {
        val newUsername = binding.etUsername.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (newUsername.isEmpty()) {
            binding.etUsername.error = "Username required"
            return
        }

        showLoading(true)

        firestore.collection("users")
            .whereEqualTo("username", newUsername)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usernameTaken = querySnapshot.any { it.id != uid }

                if (usernameTaken) {
                    showLoading(false)
                    binding.etUsername.error = "Username already taken"
                } else {
                    if (selectedImageUri != null) {
                        uploadImageAndSave(newUsername)
                    } else {
                        updateFirestore(newUsername, null)
                    }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Failed to check username", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageAndSave(newUsername: String) {
        val uid = auth.currentUser?.uid ?: return
        val fileRef = storage.child("profile_images/$uid-${UUID.randomUUID()}.jpg")

        selectedImageUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val data = baos.toByteArray()

            fileRef.putBytes(data)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateFirestore(newUsername, downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFirestore(username: String, profilePhotoUrl: String?) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>("username" to username)
        if (!profilePhotoUrl.isNullOrEmpty()) updates["profilePhoto"] = profilePhotoUrl

        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    val user = UserEntity(
                        uid = uid,
                        email = auth.currentUser?.email ?: "",
                        username = username,
                        profilePhotoUrl = profilePhotoUrl
                    )
                    userDao.insertUser(user)
                }

                showLoading(false)
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveChanges.isEnabled = !show
        binding.btnChooseImage.isEnabled = !show
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.ivPreview.setImageURI(selectedImageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
