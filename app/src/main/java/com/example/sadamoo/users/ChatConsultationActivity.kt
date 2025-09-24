package com.example.sadamoo.users

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.ActivityChatConsultationBinding
import com.example.sadamoo.users.adapters.ChatAdapter
import com.example.sadamoo.users.models.ChatMessage
import com.example.sadamoo.users.models.MessageType
import java.util.*

class ChatConsultationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatConsultationBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatConsultationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctorName = intent.getStringExtra("doctor_name") ?: "Dr. Ahmad Veteriner"
        val consultationId = intent.getStringExtra("consultation_id") ?: "consultation_1"

        setupUI(doctorName)
        setupChat()
        loadInitialMessages(doctorName)
    }

    private fun setupUI(doctorName: String) {
        binding.tvDoctorName.text = doctorName
        binding.tvOnlineStatus.text = "Online"

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCall.setOnClickListener {
            Toast.makeText(this, "Fitur panggilan video - Premium feature!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAttachment.setOnClickListener {
            Toast.makeText(this, "Kirim foto hasil scan", Toast.LENGTH_SHORT).show()
            // TODO: Implement file picker
        }
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter(chatMessages)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatConsultationActivity)
            adapter = chatAdapter
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun loadInitialMessages(doctorName: String) {
        // Mock initial conversation
        val initialMessages = listOf(
            ChatMessage(
                id = "1",
                message = "Selamat siang! Saya Dr. $doctorName. Ada yang bisa saya bantu terkait kesehatan sapi Anda?",
                timestamp = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }.time,
                type = MessageType.RECEIVED,
                senderName = doctorName
            ),
            ChatMessage(
                id = "2",
                message = "Selamat siang dokter. Sapi saya baru saja di-scan dan terdeteksi Lumpy Skin Disease. Bagaimana penanganan yang tepat?",
                timestamp = Calendar.getInstance().apply { add(Calendar.MINUTE, -28) }.time,
                type = MessageType.SENT,
                senderName = "Anda"
            ),
            ChatMessage(
                id = "3",
                message = "Baik, LSD memang perlu penanganan segera. Apakah sapi sudah menunjukkan gejala benjolan di kulit?",
                timestamp = Calendar.getInstance().apply { add(Calendar.MINUTE, -25) }.time,
                type = MessageType.RECEIVED,
                senderName = doctorName
            ),
            ChatMessage(
                id = "4",
                message = "Ya dokter, sudah ada beberapa benjolan di bagian leher dan punggung. Ukurannya sekitar 3-4 cm.",
                timestamp = Calendar.getInstance().apply { add(Calendar.MINUTE, -23) }.time,
                type = MessageType.SENT,
                senderName = "Anda"
            ),
            ChatMessage(
                id = "5",
                message = "Saya akan berikan rekomendasi pengobatan. Pertama, isolasi sapi dari ternak lain. Kedua, berikan perawatan suportif dengan antibiotik untuk mencegah infeksi sekunder.",
                timestamp = Calendar.getInstance().apply { add(Calendar.MINUTE, -20) }.time,
                type = MessageType.RECEIVED,
                senderName = doctorName
            )
        )

        chatMessages.addAll(initialMessages)
        chatAdapter.notifyDataSetChanged()
        scrollToBottom()
    }

    private fun sendMessage(message: String) {
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            timestamp = Date(),
            type = MessageType.SENT,
            senderName = "Anda"
        )

        chatMessages.add(newMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()

        // Simulate doctor reply after 2 seconds
        binding.rvChat.postDelayed({
            simulateDoctorReply(message)
        }, 2000)
    }

    private fun simulateDoctorReply(userMessage: String) {
        val doctorName = binding.tvDoctorName.text.toString()

        val reply = when {
            userMessage.lowercase().contains("obat") -> "Untuk pengobatan LSD, saya rekomendasikan pemberian antibiotik spektrum luas seperti Oxytetracycline. Dosis 10-20 mg/kg BB, diberikan secara intramuskular selama 3-5 hari."
            userMessage.lowercase().contains("makanan") || userMessage.lowercase().contains("pakan") -> "Berikan pakan berkualitas tinggi dengan protein cukup. Tambahkan vitamin A, C, dan E untuk meningkatkan imunitas. Pastikan air minum selalu bersih dan tersedia."
            userMessage.lowercase().contains("berapa lama") -> "Proses penyembuhan LSD biasanya memerlukan waktu 2-4 minggu dengan perawatan yang tepat. Benjolan akan mengering dan rontok secara bertahap."
            userMessage.lowercase().contains("biaya") -> "Estimasi biaya pengobatan LSD sekitar Rp 500.000 - Rp 1.500.000 tergantung tingkat keparahan dan obat yang digunakan."
            userMessage.lowercase().contains("terima kasih") -> "Sama-sama! Jangan ragu untuk menghubungi saya jika ada perkembangan atau pertanyaan lain. Semoga sapi Anda segera sembuh."
            else -> "Baik, saya catat keluhan Anda. Untuk kasus ini, saya sarankan untuk melakukan observasi ketat dan segera hubungi saya jika ada perubahan kondisi."
        }

        val doctorMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = reply,
            timestamp = Date(),
            type = MessageType.RECEIVED,
            senderName = doctorName
        )

        chatMessages.add(doctorMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            binding.rvChat.scrollToPosition(chatMessages.size - 1)
        }
    }
}
