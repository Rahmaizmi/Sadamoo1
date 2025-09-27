package com.example.sadamoo.users.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.databinding.DialogPaymentBinding
import com.example.sadamoo.users.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PaymentDialogFragment : DialogFragment() {
    private lateinit var binding: DialogPaymentBinding
    private lateinit var packageName: String
    private lateinit var price: String

    companion object {
        fun newInstance(packageName: String, price: String): PaymentDialogFragment {
            val fragment = PaymentDialogFragment()
            val args = Bundle()
            args.putString("package_name", packageName)
            args.putString("price", price)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            packageName = it.getString("package_name") ?: ""
            price = it.getString("price") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentInfo()
        setupPaymentButtons()
    }

    private fun setupPaymentInfo() {
        binding.tvPackageName.text = "Paket $packageName"
        binding.tvPrice.text = price
        binding.tvPaymentCode.text = generatePaymentCode()
    }

    private fun generatePaymentCode(): String {
        return "SADA${System.currentTimeMillis().toString().takeLast(6)}"
    }

    private fun setupPaymentButtons() {
        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }

        binding.btnCancelPayment.setOnClickListener {
            dismiss()
        }
    }

    private fun processPayment() {
        // Simulate payment processing
        binding.btnConfirmPayment.isEnabled = false
        binding.btnConfirmPayment.text = "Memproses..."

        // Simulate 2 second delay
        binding.root.postDelayed({
            updateUserSubscription()
        }, 2000)
    }

    private fun updateUserSubscription() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val firestore = FirebaseFirestore.getInstance()

            // Calculate subscription end date based on package
            val calendar = Calendar.getInstance()
            when (packageName) {
                "Basic" -> calendar.add(Calendar.MONTH, 1)
                "Standard" -> calendar.add(Calendar.MONTH, 3)
                "Premium" -> calendar.add(Calendar.MONTH, 6)
                "Ultimate" -> calendar.add(Calendar.YEAR, 1)
            }

            val subscriptionData = hashMapOf(
                "subscriptionStatus" to "active",
                "subscriptionType" to packageName,
                "subscriptionStartDate" to com.google.firebase.Timestamp.now(),
                "subscriptionEndDate" to com.google.firebase.Timestamp(calendar.time),
                "paymentAmount" to price,
                "paymentDate" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("users").document(currentUser.uid)
                .update(subscriptionData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Pembayaran berhasil! Selamat menikmati fitur premium.", Toast.LENGTH_LONG).show()
                    dismiss()

                    // Refresh parent activity
                    (activity as? ProfileActivity)?.loadUserProfile()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memproses pembayaran. Coba lagi.", Toast.LENGTH_SHORT).show()
                    binding.btnConfirmPayment.isEnabled = true
                    binding.btnConfirmPayment.text = "Konfirmasi Pembayaran"
                }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
