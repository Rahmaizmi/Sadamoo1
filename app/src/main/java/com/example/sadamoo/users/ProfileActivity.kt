package com.example.sadamoo.users

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.LoginActivity
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityProfileBinding
import com.example.sadamoo.users.dialogs.UpgradeDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.example.sadamoo.utils.applyStatusBarPadding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        binding.root.applyStatusBarPadding()
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupBottomNavigation()
        loadUserProfile()
        setupClickListeners()
    }

    fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Load basic user info
            binding.tvUserEmail.text = currentUser.email

            // Load detailed user info from Firestore
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "User"
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val trialStartDate = document.getTimestamp("trialStartDate")

                        binding.tvUserName.text = userName
                        updateSubscriptionStatus(subscriptionStatus, trialStartDate)
                        loadUserStatistics(currentUser.uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateSubscriptionStatus(status: String, trialStartDate: com.google.firebase.Timestamp?) {
        when (status) {
            "trial" -> {
                binding.tvSubscriptionStatus.text = "Trial"
                binding.tvSubscriptionStatus.background = getDrawable(R.drawable.subscription_badge_trial)

                if (trialStartDate != null) {
                    val daysLeft = calculateTrialDaysLeft(trialStartDate)
                    binding.tvTrialDaysLeft.text = if (daysLeft > 0) "$daysLeft hari tersisa" else "Trial berakhir"
                    binding.tvTrialDaysLeft.setTextColor(if (daysLeft > 2) Color.parseColor("#FF5722") else Color.parseColor("#F44336"))
                }

                // Show upgrade card
                binding.cardSubscription.visibility = android.view.View.VISIBLE
            }
            "active" -> {
                binding.tvSubscriptionStatus.text = "Premium"
                binding.tvSubscriptionStatus.background = getDrawable(R.drawable.subscription_badge_premium)
                binding.tvTrialDaysLeft.text = "Aktif"
                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#4CAF50"))

                // Hide upgrade card
                binding.cardSubscription.visibility = android.view.View.GONE
            }
            "expired" -> {
                binding.tvSubscriptionStatus.text = "Berakhir"
                binding.tvSubscriptionStatus.background = getDrawable(R.drawable.subscription_badge_expired)
                binding.tvTrialDaysLeft.text = "Perlu diperpanjang"
                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#F44336"))

                // Show upgrade card
                binding.cardSubscription.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun calculateTrialDaysLeft(trialStartDate: com.google.firebase.Timestamp): Int {
        val currentTime = System.currentTimeMillis()
        val trialStart = trialStartDate.toDate().time
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        val timeLeft = (trialStart + sevenDaysInMillis) - currentTime

        return if (timeLeft > 0) (timeLeft / (24 * 60 * 60 * 1000L)).toInt() else 0
    }

    private fun loadUserStatistics(userId: String) {
        // Load scan statistics from Firestore
        // In real app, you would query the scan_history collection
        firestore.collection("scan_history")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val totalScans = documents.size()
                var healthyCattle = 0
                var diseasedCattle = 0

                for (document in documents) {
                    val diseaseDetected = document.getString("diseaseDetected") ?: ""
                    if (diseaseDetected == "Tidak ada penyakit" || diseaseDetected.contains("sehat", ignoreCase = true)) {
                        healthyCattle++
                    } else {
                        diseasedCattle++
                    }
                }

                // Update UI
                binding.tvTotalScans.text = totalScans.toString()
                binding.tvHealthyCattle.text = healthyCattle.toString()
                binding.tvDiseasedCattle.text = diseasedCattle.toString()
            }
            .addOnFailureListener {
                // Set default values if query fails
                binding.tvTotalScans.text = "2"
                binding.tvHealthyCattle.text = "1"
                binding.tvDiseasedCattle.text = "1"
            }
    }

    private fun setupClickListeners() {
        // Edit Profile Button
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Upgrade Premium Button
        binding.btnUpgradePremium.setOnClickListener {
            showUpgradeDialog()
        }

        // Menu Items
        binding.menuEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuSubscription.setOnClickListener {
            showSubscriptionManagement()
        }

        binding.menuSettings.setOnClickListener {
            Toast.makeText(this, "Pengaturan - Coming Soon!", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.menuHelp.setOnClickListener {
            Toast.makeText(this, "Bantuan & Dukungan - Coming Soon!", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, HelpActivity::class.java))
        }

        binding.menuLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showUpgradeDialog() {
        val dialog = UpgradeDialogFragment()
        dialog.show(supportFragmentManager, "UpgradeDialog")
    }

    private fun showSubscriptionManagement() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val subscriptionType = document.getString("subscriptionType") ?: ""
                        val subscriptionEndDate = document.getTimestamp("subscriptionEndDate")

                        showSubscriptionDetails(subscriptionStatus, subscriptionType, subscriptionEndDate)
                    }
                }
        }
    }

    private fun showSubscriptionDetails(status: String, type: String, endDate: com.google.firebase.Timestamp?) {
        val message = when (status) {
            "trial" -> "Anda sedang dalam masa trial 7 hari gratis."
            "active" -> {
                val endDateStr = if (endDate != null) {
                    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(endDate.toDate())
                } else "Tidak diketahui"
                "Langganan $type aktif hingga $endDateStr"
            }
            "expired" -> "Langganan Anda telah berakhir. Upgrade untuk melanjutkan akses premium."
            else -> "Status langganan tidak diketahui."
        }

        AlertDialog.Builder(this)
            .setTitle("Status Langganan")
            .setMessage(message)
            .setPositiveButton("Upgrade") { _, _ ->
                showUpgradeDialog()
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()

        // Clear any cached data if needed
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        setActiveNav(binding.navProfil)

        binding.navBeranda.setOnClickListener {
            finish() // Kembali ke MainActivity
        }

        binding.navInformasi.setOnClickListener {
            startActivity(Intent(this, InformationActivity::class.java))
        }

        binding.fabDeteksi.setOnClickListener {
            startActivity(Intent(this, CameraScanActivity::class.java))
        }

        binding.navRiwayat.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.navProfil.setOnClickListener {
            setActiveNav(binding.navProfil)
        }
    }

    private fun setActiveNav(activeNav: LinearLayout) {
        val allNavs = listOf(binding.navBeranda, binding.navInformasi, binding.navRiwayat, binding.navProfil)
        val activeColor = Color.parseColor("#4A90E2")
        val inactiveColor = Color.parseColor("#B0B0B0")

        for (nav in allNavs) {
            val icon = nav.getChildAt(0) as ImageView
            val label = nav.getChildAt(1) as TextView

            if (nav == activeNav) {
                icon.setColorFilter(activeColor)
                label.setTextColor(activeColor)
            } else {
                icon.setColorFilter(inactiveColor)
                label.setTextColor(inactiveColor)
            }
        }
    }
}