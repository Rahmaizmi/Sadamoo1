package com.example.sadamoo

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.admin.AdminActivity
import com.example.sadamoo.databinding.ActivityLoginBinding
import com.example.sadamoo.users.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validasi input
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Login dengan Firebase Auth
            loginUser(email, password)
        }

        binding.tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        // Show loading
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Masuk..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Cek role user di Firestore
                        checkUserRole(it.uid)
                    }
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("network error") == true ->
                            "Periksa koneksi internet Anda"
                        task.exception?.message?.contains("no user record") == true ->
                            "Email tidak terdaftar"
                        task.exception?.message?.contains("wrong password") == true ->
                            "Password salah"
                        task.exception?.message?.contains("too many requests") == true ->
                            "Terlalu banyak percobaan. Coba lagi nanti"
                        else -> "Login gagal: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserRole(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "user"

                    when (role) {
                        "admin" -> {
                            Toast.makeText(this, "Login berhasil sebagai Admin!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, AdminActivity::class.java))
                        }
                        "user" -> {
                            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Data user tidak ditemukan!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
