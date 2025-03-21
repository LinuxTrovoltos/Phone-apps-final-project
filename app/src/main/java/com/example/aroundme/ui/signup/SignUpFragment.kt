package com.example.aroundme.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aroundme.databinding.FragmentSignUpBinding
import com.example.aroundme.ui.signup.SignUpFragmentDirections
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener { registerUser() }
        binding.tvLogin.setOnClickListener {
            // Navigate from SignUp to Login using Safe Args
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show()
                // Navigate from SignUp to Home using Safe Args
                val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                findNavController().navigate(action)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Sign Up Failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
