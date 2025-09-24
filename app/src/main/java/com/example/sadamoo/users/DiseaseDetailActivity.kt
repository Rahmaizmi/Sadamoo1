package com.example.sadamoo.users

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityDiseaseDetailBinding
import com.example.sadamoo.users.models.Disease

class DiseaseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiseaseDetailBinding
    private lateinit var disease: Disease

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val diseaseId = intent.getStringExtra("disease_id")
        if (diseaseId != null) {
            loadDiseaseDetail(diseaseId)
            setupUI()
            setupBottomNavigation()
        } else {
            finish()
        }
    }

    private fun loadDiseaseDetail(diseaseId: String) {
        // Load disease data berdasarkan ID
        disease = when (diseaseId) {
            "cacingan" -> Disease(
                id = "cacingan",
                name = "Cacingan",
                scientificName = "Helminthiasis",
                description = "Penyakit yang disebabkan oleh infeksi cacing parasit pada saluran pencernaan sapi yang dapat mengganggu penyerapan nutrisi dan pertumbuhan.",
                symptoms = listOf(
                    "Nafsu makan menurun",
                    "Berat badan turun drastis",
                    "Bulu kusam dan kasar",
                    "Diare berulang",
                    "Perut membesar (ascites)",
                    "Anemia (pucat pada selaput mata)",
                    "Pertumbuhan terhambat"
                ),
                causes = listOf(
                    "Konsumsi pakan atau air yang terkontaminasi telur cacing",
                    "Sanitasi kandang yang buruk",
                    "Kepadatan ternak yang tinggi",
                    "Sistem pemeliharaan yang tidak higienis",
                    "Kurangnya program deworming rutin"
                ),
                treatment = listOf(
                    "Pemberian obat cacing (antelmintik) sesuai dosis dokter hewan",
                    "Albendazole 10-15 mg/kg berat badan",
                    "Ivermectin 0.2 mg/kg berat badan",
                    "Perbaikan nutrisi dengan pakan berkualitas",
                    "Pemberian vitamin dan mineral tambahan",
                    "Isolasi sementara hewan yang terinfeksi"
                ),
                prevention = listOf(
                    "Deworming rutin setiap 3-6 bulan",
                    "Menjaga kebersihan kandang dan lingkungan",
                    "Pemberian pakan dan air bersih",
                    "Rotasi padang penggembalaan",
                    "Pemeriksaan feses berkala",
                    "Karantina hewan baru sebelum digabung"
                ),
                severity = "Sedang",
                imageRes = R.drawable.disease_cacingan,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau", "Kambing", "Domba")
            )

            "pmk" -> Disease(
                id = "pmk",
                name = "Penyakit Mulut dan Kuku (PMK)",
                scientificName = "Foot and Mouth Disease (FMD)",
                description = "Penyakit virus akut yang sangat menular pada hewan berkuku belah, ditandai dengan lepuh dan luka pada mulut, lidah, dan kuku.",
                symptoms = listOf(
                    "Lepuh berisi cairan pada lidah, gusi, dan hidung",
                    "Luka pada kuku dan sela-sela kuku",
                    "Hewan pincang dan sulit berjalan",
                    "Air liur berlebihan (hipersalivasi)",
                    "Demam tinggi (40-41°C)",
                    "Nafsu makan hilang",
                    "Penurunan produksi susu drastis",
                    "Kelemahan dan depresi"
                ),
                causes = listOf(
                    "Infeksi virus Foot and Mouth Disease Virus (FMDV)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi pakan, air, atau peralatan",
                    "Penyebaran melalui udara (aerosol)",
                    "Kendaraan dan manusia sebagai pembawa virus"
                ),
                treatment = listOf(
                    "Tidak ada pengobatan spesifik untuk virus PMK",
                    "Perawatan suportif untuk mencegah infeksi sekunder",
                    "Pemberian antibiotik untuk mencegah infeksi bakteri",
                    "Perawatan luka dengan antiseptik",
                    "Pemberian cairan dan elektrolit",
                    "Isolasi ketat hewan terinfeksi",
                    "Pelaporan wajib ke Dinas Peternakan"
                ),
                prevention = listOf(
                    "Vaksinasi rutin sesuai program pemerintah",
                    "Biosekuriti ketat di peternakan",
                    "Karantina hewan baru minimal 21 hari",
                    "Desinfeksi kendaraan dan peralatan",
                    "Pembatasan lalu lintas ternak",
                    "Monitoring kesehatan hewan rutin",
                    "Pelaporan kasus mencurigakan segera"
                ),
                severity = "Berat",
                imageRes = R.drawable.disease_pmk,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau", "Babi", "Kambing", "Domba")
            )

            "lsd" -> Disease(
                id = "lsd",
                name = "Lumpy Skin Disease (LSD)",
                scientificName = "Lumpy Skin Disease",
                description = "Penyakit virus yang menyerang sapi dan kerbau, ditandai dengan benjolan-benjolan pada kulit yang dapat menyebabkan kerugian ekonomi signifikan.",
                symptoms = listOf(
                    "Benjolan keras (nodul) berdiameter 2-5 cm pada kulit",
                    "Demam tinggi hingga 41°C",
                    "Pembengkakan kelenjar getah bening",
                    "Nafsu makan menurun drastis",
                    "Penurunan produksi susu hingga 50%",
                    "Kerusakan kulit dan kemungkinan infeksi sekunder",
                    "Edema pada kaki, skrotum, atau ambing",
                    "Discharge dari mata dan hidung"
                ),
                causes = listOf(
                    "Infeksi Lumpy Skin Disease Virus (LSDV)",
                    "Penularan melalui vektor serangga (lalat, nyamuk, kutu)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi melalui peralatan dan pakan",
                    "Kondisi lingkungan yang mendukung perkembangan vektor"
                ),
                treatment = listOf(
                    "Tidak ada pengobatan spesifik untuk virus LSD",
                    "Perawatan suportif dan simptomatik",
                    "Antibiotik untuk mencegah infeksi sekunder",
                    "Anti-inflamasi untuk mengurangi peradangan",
                    "Perawatan luka dan benjolan dengan antiseptik",
                    "Pemberian vitamin dan mineral untuk meningkatkan imunitas",
                    "Isolasi hewan terinfeksi"
                ),
                prevention = listOf(
                    "Vaksinasi dengan vaksin LSD yang tersedia",
                    "Pengendalian vektor serangga secara intensif",
                    "Karantina hewan baru minimal 28 hari",
                    "Biosekuriti ketat di peternakan",
                    "Monitoring kesehatan hewan rutin",
                    "Desinfeksi kandang dan peralatan",
                    "Pelaporan kasus mencurigakan ke otoritas"
                ),
                severity = "Berat",
                imageRes = R.drawable.disease_lsd,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau")
            )

            else -> return // Invalid disease ID
        }
    }

    private fun setupUI() {
        binding.apply {
            // Header info
            tvDiseaseName.text = disease.name
            tvScientificName.text = disease.scientificName
            tvDescription.text = disease.description
            ivDiseaseImage.setImageResource(disease.imageRes)

            // Severity and contagious status
            tvSeverity.text = disease.severity
            tvContagious.text = if (disease.isContagious) "Menular" else "Tidak Menular"

            // Set severity color
            val severityColor = when (disease.severity) {
                "Ringan" -> android.R.color.holo_green_dark
                "Sedang" -> android.R.color.holo_orange_dark
                "Berat" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            tvSeverity.setTextColor(getColor(severityColor))
            tvContagious.setTextColor(
                if (disease.isContagious) getColor(android.R.color.holo_red_dark)
                else getColor(android.R.color.holo_green_dark)
            )

            // Populate lists
            populateSymptoms()
            populateCauses()
            populateTreatment()
            populatePrevention()
            populateAffectedAnimals()
        }
    }

    private fun populateSymptoms() {
        binding.layoutSymptoms.removeAllViews()
        disease.symptoms.forEach { symptom ->
            val textView = createBulletTextView(symptom)
            binding.layoutSymptoms.addView(textView)
        }
    }

    private fun populateCauses() {
        binding.layoutCauses.removeAllViews()
        disease.causes.forEach { cause ->
            val textView = createBulletTextView(cause)
            binding.layoutCauses.addView(textView)
        }
    }

    private fun populateTreatment() {
        binding.layoutTreatment.removeAllViews()
        disease.treatment.forEach { treatment ->
            val textView = createBulletTextView(treatment)
            binding.layoutTreatment.addView(textView)
        }
    }

    private fun populatePrevention() {
        binding.layoutPrevention.removeAllViews()
        disease.prevention.forEach { prevention ->
            val textView = createBulletTextView(prevention)
            binding.layoutPrevention.addView(textView)
        }
    }

    private fun populateAffectedAnimals() {
        binding.layoutAffectedAnimals.removeAllViews()
        disease.affectedAnimals.forEach { animal ->
            val textView = createBulletTextView(animal)
            binding.layoutAffectedAnimals.addView(textView)
        }
    }

    private fun createBulletTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = "• $text"
            textSize = 14f
            setTextColor(getColor(android.R.color.black))
            setPadding(0, 8, 0, 8)
            typeface = resources.getFont(R.font.quicksand)
        }
    }

    private fun setupBottomNavigation() {
        setActiveNav(binding.navInformasi)

        binding.navBeranda.setOnClickListener {
            finish()
        }

        binding.navInformasi.setOnClickListener {
            finish()
        }

        binding.navRiwayat.setOnClickListener {
            setActiveNav(binding.navRiwayat)
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
