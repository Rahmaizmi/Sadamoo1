package com.example.sadamoo.users

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.databinding.ActivityScanResultBinding
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding

    // 🔹 Hasil scan untuk sapi berpenyakit LSD
    private val manualCattleType = "Sapi Ongole"
    private val manualConfidence = 0.87f
    private val manualHealthStatus = "Terdeteksi Penyakit LSD (Lumpy Skin Disease)"
    private val diseaseDetection = "Lumpy Skin Disease (LSD)"

    // Data penyakit LSD
    private val diseaseSymptoms = """
        • Benjolan keras (nodul) berdiameter 2-5 cm pada kulit
        • Demam tinggi (40-41°C)
        • Penurunan nafsu makan drastis
        • Kelenjar getah bening membengkak
        • Luka terbuka yang tidak kunjung sembuh
        • Penurunan produksi susu hingga 50%
        • Mata berair dan hidung berlendir
        • Kesulitan bernapas
    """.trimIndent()

    private val diseaseSolution = """
        1. ISOLASI SEGERA sapi yang terinfeksi
        2. Hubungi dokter hewan untuk diagnosis lanjutan
        3. Berikan antibiotik sesuai resep dokter
        4. Lakukan perawatan luka dengan antiseptik
        5. Berikan pakan berkualitas tinggi dan vitamin
        6. Vaksinasi sapi sehat di sekitar area
        7. Disinfeksi kandang dan peralatan
        8. Kontrol vektor (lalat, nyamuk, kutu)
    """.trimIndent()

    private val economicLoss = """
        KERUGIAN JIKA TIDAK SEGERA DIATASI:
        
        📉 Kerugian Ekonomi:
        • Penurunan produksi susu 30-50%
        • Penurunan berat badan 20-30%
        • Biaya pengobatan meningkat 3-5x lipat
        • Risiko kematian hingga 10-20%
        
        📊 Dampak Jangka Panjang:
        • Penyebaran ke ternak lain (sangat menular)
        • Embargo perdagangan ternak
        • Kerugian finansial Rp 5-15 juta per ekor
        • Gangguan reproduksi dan kemandulan
    """.trimIndent()

    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Tambahkan padding agar tidak ketiban status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        // Ambil gambar dari intent
        imageUriString = intent.getStringExtra("image")

        if (imageUriString != null) {
            val imageFile = File(imageUriString!!)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                binding.ivScannedImage.setImageBitmap(bitmap)
            }
        }

        // Tampilkan hasil scan
        setupScanResults()

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun setupScanResults() {
        val isFromHistory = intent.getBooleanExtra("is_from_history", false)

        if (isFromHistory) {
            // Ambil data dari history
            val cattleType = intent.getStringExtra("cattle_type") ?: "Tidak diketahui"
            val confidence = intent.getFloatExtra("confidence_score", 0f)
            val healthStatus = intent.getStringExtra("description") ?: "-"

            binding.tvCattleType.text = cattleType
            binding.tvConfidence.text = "${"%.2f".format(confidence * 100)}%"
            binding.tvHealthStatus.text = healthStatus
        } else {
            // Pakai hasil manual untuk LSD
            binding.tvCattleType.text = manualCattleType
            binding.tvConfidence.text = "${"%.2f".format(manualConfidence * 100)}%"
            binding.tvHealthStatus.text = manualHealthStatus
        }
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
                putExtra("disease_info", "Sapi terdeteksi LSD - butuh penanganan segera!")
            }
            startActivity(intent)
        }
    }

    private fun saveToDatabase() {
        val dao = DetectionRoomDatabase.getDatabase(applicationContext).detectionDao()

        val detection = Detection(
            uri = imageUriString ?: "",
            disease_name = diseaseDetection,
            description = manualHealthStatus,
            confidence = manualConfidence,
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
