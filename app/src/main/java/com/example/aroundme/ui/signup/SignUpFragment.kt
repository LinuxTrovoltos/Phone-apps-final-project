package com.example.aroundme.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aroundme.R
import com.example.aroundme.databinding.FragmentSignUpBinding
import com.example.aroundme.ui.signup.SignUpFragmentDirections
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        binding.btnSignUp.setOnClickListener { registerUser() }
        binding.btnGoogleSignUp.setOnClickListener { signUpWithGoogle() }
        binding.tvLogin.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupGoogleSignIn() {
        val webClientId = getString(R.string.web_client_id)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(context, "Google Sign-Up Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    saveUserToFirestore(user, fromGoogle = true)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Reset previous errors
        binding.etEmail.error = null
        binding.etPassword.error = null
        binding.etConfirmPassword.error = null

        // **Validations**
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return
        }
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Confirm Password cannot be empty"
            return
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        // Firebase Sign-Up
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    saveUserToFirestore(user, fromGoogle = false)
                }
            }
            .addOnFailureListener { e ->
                when {
                    e.message?.contains("email address is already in use") == true ->
                        binding.etEmail.error = "Email is already registered"

                    e.message?.contains("badly formatted") == true ->
                        binding.etEmail.error = "Invalid email format"

                    e.message?.contains("password is invalid") == true ->
                        binding.etPassword.error = "Password must be at least 6 characters"

                    else ->
                        Toast.makeText(context, "Sign Up Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, fromGoogle: Boolean) {
        val userId = user.uid
        val email = user.email ?: ""
        val username = generateUsername(email)
        val profilePhoto = if (fromGoogle) {
            user.photoUrl?.toString() ?: DEFAULT_PROFILE_PIC
        } else {
            DEFAULT_PROFILE_PIC
        }

        val userMap = hashMapOf(
            "email" to email,
            "username" to username,
            "profilePhoto" to profilePhoto
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {

                findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error saving user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateUsername(email: String): String {
        return email.substringBefore("@") + (1000..9999).random()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_PROFILE_PIC = "android.resource://com.example.aroundme/drawable/default_profile"
    }
}
