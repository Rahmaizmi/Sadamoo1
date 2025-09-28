package com.example.sadamoo.users

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivProfileAvatar.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadCurrentUserData()
        setupClickListeners()
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.etEmail.setText(currentUser.email)

            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etName.setText(document.getString("name") ?: "")
                        binding.etPhone.setText(document.getString("phone") ?: "")
                        binding.etAddress.setText(document.getString("address") ?: "")
                        binding.etFarmName.setText(document.getString("farmName") ?: "")
                        binding.etCattleCount.setText(document.getLong("cattleCount")?.toString() ?: "")
                    }
                }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.ivProfileAvatar.setOnClickListener {
            openImagePicker()
        }

        binding.btnChangePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val farmName = binding.etFarmName.text.toString().trim()
        val cattleCountStr = binding.etCattleCount.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            return
        }

        val cattleCount = cattleCountStr.toLongOrNull() ?: 0

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Menyimpan..."

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "address" to address,
                "farmName" to farmName,
                "cattleCount" to cattleCount,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("users").document(currentUser.uid)
                .update(userData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Simpan"
                }
        }
    }
}
