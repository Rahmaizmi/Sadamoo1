package com.example.sadamoo.users

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityScanResultBinding
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding

    private var imageUriString: String? = null
    private var cattleType: String = ""
    private var confidence: Float = 0f
    private var isHealthy: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari intent
        imageUriString = intent.getStringExtra("image")
        cattleType = intent.getStringExtra("cattle_type") ?: "Sapi Tidak Dikenal"
        confidence = intent.getFloatExtra("confidence_score", 0f)
        isHealthy = intent.getBooleanExtra("is_healthy", true)


        // Tampilkan gambar
        // Tampilkan gambar
        if (imageUriString != null) {
            val imageFile = File(imageUriString)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                binding.ivScannedImage.setImageBitmap(bitmap)
            }
        }


        // Tampilkan hasil scan
        setupScanResults(cattleType, confidence, isHealthy)

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun setupScanResults(cattleType: String, confidence: Float, isHealthy: Boolean) {
        // Tampilkan jenis sapi + status kesehatan
        val healthStatus = if (isHealthy) "Sapi Anda Sehat" else ""
        binding.tvCattleType.text = "$cattleType"

        // Tambahkan confidence score (opsional)
        binding.tvConfidence.text = "${"%.2f".format(confidence * 100)}%"
        binding.tvHealthStatus.text = "$healthStatus"
    }

    private fun setupBottomNavigation() {
        binding.navBack.setOnClickListener {
            finish()
        }

        binding.navSave.setOnClickListener {
            Toast.makeText(this, "Hasil scan disimpan ke riwayat", Toast.LENGTH_SHORT).show()
            saveToDatabase()
        }

        binding.navConsultation.setOnClickListener {
            val intent = Intent(this, ChatConsultationActivity::class.java).apply {
                putExtra("doctor_name", "Dr. Ahmad Veteriner")
                putExtra("consultation_id", "new_consultation")
            }
            startActivity(intent)
        }
    }

    private fun saveToDatabase() {
        val dao = DetectionRoomDatabase.getDatabase(applicationContext).detectionDao()

        val detection = Detection(
            uri = imageUriString ?: "",
            disease_name = cattleType,
            description = if (isHealthy) "Sehat" else "Perlu pemeriksaan lanjut",
            confidence = confidence,
            detectedAt = getCurrentDateTime()
        )

        lifecycleScope.launch {
            dao.insert(detection)
            runOnUiThread {
                Toast.makeText(this@ScanResultActivity, "Disimpan ke riwayat", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
