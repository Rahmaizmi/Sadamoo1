package com.example.sadamoo.users

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityHistoryBinding
import com.example.sadamoo.users.adapters.HistoryAdapter
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import com.example.sadamoo.users.dialogs.AdvancedFilterDialog

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private var allHistory = listOf<Detection>()
    private var filteredHistory = listOf<Detection>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
//        setupHistoryData()
        loadHistoryFromDb()
        setupRecyclerView()
        setupSearch()
        setupFilters()
    }

    private fun loadHistoryFromDb() {
        val dao = DetectionRoomDatabase.getDatabase(this).detectionDao()
        dao.getAllDetections().observe(this, Observer { detections ->
            allHistory = detections
            filteredHistory = allHistory
            historyAdapter.updateData(filteredHistory)
            updateEmptyState()
        })
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(filteredHistory) { detection ->
            val intent = Intent(this, ScanResultActivity::class.java).apply {
                putExtra("image", detection.uri)
                putExtra("cattle_type", detection.disease_name ?: "Tidak diketahui")
                putExtra("description", detection.description ?: "")
                putExtra("confidence_score", detection.confidence)
                putExtra("detected_at", detection.detectedAt)
                putExtra("is_from_history", true)
            }
            startActivity(intent)
        }

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterHistory(query, currentFilter)
            }
        })

        binding.ivFilter.setOnClickListener {
            val dialog = AdvancedFilterDialog { criteria ->
                applyAdvancedFilter(criteria)
            }
            dialog.show(supportFragmentManager, "AdvancedFilter")
        }
    } //

    private fun applyAdvancedFilter(criteria: AdvancedFilterDialog.FilterCriteria) {
        var filtered = allHistory

        // Filter by date (detectedAt Anda masih String, nanti sebaiknya pakai Date)
        if (criteria.dateRange.first != null && criteria.dateRange.second != null) {
            filtered = filtered.filter { item ->
                // TODO: convert detectedAt (String) ke Date agar bisa dibandingkan
                true
            }
        }

        // Filter by severity / disease_name (opsional sesuai kebutuhan)
        if (criteria.severity != "Semua Tingkat") {
            filtered = filtered.filter { it.confidence > 0.8f } // contoh filter sederhana
        }

        if (criteria.cattleType != "Semua Jenis") {
            filtered = filtered.filter { it.disease_name == criteria.cattleType }
        }

        filteredHistory = filtered
        historyAdapter.updateData(filteredHistory)
        updateEmptyState()

        android.widget.Toast.makeText(
            this,
            "Filter diterapkan: ${filtered.size} hasil",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterHistory(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterScan.setOnClickListener {
            setActiveFilter("scan")
            filterHistory(binding.etSearch.text.toString(), "scan")
        }

        binding.btnFilterConsultation.setOnClickListener {
            setActiveFilter("consultation")
            filterHistory(binding.etSearch.text.toString(), "consultation")
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        // Reset all buttons
        binding.btnFilterAll.background = getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterAll.setTextColor(Color.parseColor("#4A90E2"))
        binding.btnFilterScan.background = getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterScan.setTextColor(Color.parseColor("#4A90E2"))
        binding.btnFilterConsultation.background = getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterConsultation.setTextColor(Color.parseColor("#4A90E2"))

        // Set active button
        when (filter) {
            "all" -> {
                binding.btnFilterAll.background = getDrawable(R.drawable.filter_button_active)
                binding.btnFilterAll.setTextColor(Color.WHITE)
            }

            "scan" -> {
                binding.btnFilterScan.background = getDrawable(R.drawable.filter_button_active)
                binding.btnFilterScan.setTextColor(Color.WHITE)
            }

            "consultation" -> {
                binding.btnFilterConsultation.background =
                    getDrawable(R.drawable.filter_button_active)
                binding.btnFilterConsultation.setTextColor(Color.WHITE)
            }
        }
    }

    private fun filterHistory(query: String, filter: String) {
        var filtered = allHistory

        // sementara semua dianggap "scan"
        if (filter == "consultation") {
            filtered = emptyList() // tidak ada data konsultasi
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter { item ->
                item.disease_name?.lowercase()?.contains(query) == true ||
                        item.description?.lowercase()?.contains(query) == true ||
                        item.detectedAt.lowercase().contains(query)
            }
        }

        filteredHistory = filtered
        historyAdapter.updateData(filteredHistory)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredHistory.isEmpty()) {
            binding.rvHistory.visibility = android.view.View.GONE
            binding.layoutEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.rvHistory.visibility = android.view.View.VISIBLE
            binding.layoutEmptyState.visibility = android.view.View.GONE
        }
    }

    private fun setupBottomNavigation() {
        setActiveNav(binding.navRiwayat)

        binding.navBeranda.setOnClickListener {
            finish()
        }

        binding.navInformasi.setOnClickListener {
            startActivity(Intent(this, InformationActivity::class.java))
        }

        binding.fabDeteksi.setOnClickListener {
            startActivity(Intent(this, CameraScanActivity::class.java))
        }

        binding.navRiwayat.setOnClickListener {
            setActiveNav(binding.navRiwayat)
        }

        binding.navProfil.setOnClickListener {
            // startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setActiveNav(activeNav: LinearLayout) {
        val allNavs =
            listOf(binding.navBeranda, binding.navInformasi, binding.navRiwayat, binding.navProfil)
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

