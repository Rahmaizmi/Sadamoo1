package com.example.sadamoo.users.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.databinding.DialogUpgradeBinding

class UpgradeDialogFragment : DialogFragment() {
    private lateinit var binding: DialogUpgradeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUpgradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPackageButtons()
        setupCloseButton()
    }

    private fun setupPackageButtons() {
        // Basic Package
        binding.btnBasicPackage.setOnClickListener {
            selectPackage("Paket 1 -", "Rp 25.000", "1 Minggu")
        }

        // Standard Package
        binding.btnStandardPackage.setOnClickListener {
            selectPackage("Paket 2 -", "Rp 50.000", "1 Bulan")
        }

        // Premium Package
        binding.btnPremiumPackage.setOnClickListener {
            selectPackage("Paket - 3", "Rp 100.000", "1 Tahun")
        }

    }

    private fun selectPackage(packageName: String, price: String, duration: String) {
        Toast.makeText(requireContext(), "Paket $packageName dipilih - $price untuk $duration", Toast.LENGTH_LONG).show()

        // TODO: Implement payment gateway integration
        // For now, just show success message
        showPaymentDialog(packageName, price)
    }

    private fun showPaymentDialog(packageName: String, price: String) {
        val paymentDialog = PaymentDialogFragment.newInstance(packageName, price)
        paymentDialog.show(parentFragmentManager, "PaymentDialog")
        dismiss()
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnLater.setOnClickListener {
            Toast.makeText(requireContext(), "Anda dapat upgrade kapan saja di menu Profil", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
