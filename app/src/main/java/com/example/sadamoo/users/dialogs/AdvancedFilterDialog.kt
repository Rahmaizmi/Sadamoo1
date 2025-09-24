package com.example.sadamoo.users.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.R
import com.example.sadamoo.databinding.DialogAdvancedFilterBinding
import java.text.SimpleDateFormat
import java.util.*

class AdvancedFilterDialog(
    private val onFilterApplied: (FilterCriteria) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAdvancedFilterBinding
    private var startDate: Date? = null
    private var endDate: Date? = null

    data class FilterCriteria(
        val dateRange: Pair<Date?, Date?>,
        val status: String,
        val severity: String,
        val cattleType: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAdvancedFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupDatePickers()
        setupButtons()
    }

    private fun setupSpinners() {
        // Status Spinner
        val statusOptions = arrayOf("Semua Status", "Selesai", "Berlangsung", "Menunggu")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Severity Spinner
        val severityOptions = arrayOf("Semua Tingkat", "Sehat", "Ringan", "Sedang", "Berat")
        val severityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityOptions)
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSeverity.adapter = severityAdapter

        // Cattle Type Spinner
        val cattleOptions = arrayOf("Semua Jenis", "Sapi Brahman", "Sapi Limosin", "Sapi Madura", "Sapi Simental", "Sapi Bali")
        val cattleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cattleOptions)
        cattleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCattleType.adapter = cattleAdapter
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        binding.btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    startDate = calendar.time
                    binding.btnStartDate.text = dateFormat.format(startDate!!)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    endDate = calendar.time
                    binding.btnEndDate.text = dateFormat.format(endDate!!)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnApplyFilter.setOnClickListener {
            val criteria = FilterCriteria(
                dateRange = Pair(startDate, endDate),
                status = binding.spinnerStatus.selectedItem.toString(),
                severity = binding.spinnerSeverity.selectedItem.toString(),
                cattleType = binding.spinnerCattleType.selectedItem.toString()
            )
            onFilterApplied(criteria)
            dismiss()
        }

        binding.btnResetFilter.setOnClickListener {
            // Reset all filters
            binding.spinnerStatus.setSelection(0)
            binding.spinnerSeverity.setSelection(0)
            binding.spinnerCattleType.setSelection(0)
            startDate = null
            endDate = null
            binding.btnStartDate.text = "Pilih Tanggal Mulai"
            binding.btnEndDate.text = "Pilih Tanggal Akhir"
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
