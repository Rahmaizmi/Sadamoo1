package com.example.sadamoo.users

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.ActivityChatConsultationBinding
import com.example.sadamoo.users.adapters.ChatAdapter
import com.example.sadamoo.users.models.ChatMessage
import com.example.sadamoo.users.models.MessageType
import com.example.sadamoo.utils.applyStatusBarPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ChatConsultationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatConsultationBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var chatRoomId: String = ""
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var doctorId: String = ""
    private var doctorName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatConsultationBinding.inflate(layoutInflater)
        binding.root.applyStatusBarPadding()
        setContentView(binding.root)

        currentUserId = auth.currentUser?.uid ?: ""

        // 🔹 Cari dokter dari users (role = doctor)
        db.collection("users")
            .whereEqualTo("role", "doctor")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doctorDoc = snapshot.documents[0]
                    doctorId = doctorDoc.id
                    doctorName = doctorDoc.getString("name") ?: "Dokter"

                    // generate chatRoomId unik
                    chatRoomId = if (currentUserId < doctorId) {
                        "${currentUserId}_$doctorId"
                    } else {
                        "${doctorId}_$currentUserId"
                    }

                    binding.tvDoctorName.text = doctorName
                    binding.tvOnlineStatus.text = "Online"

                    // Load nama user
                    loadCurrentUserName {
                        setupUI()
                        setupChat()
                        listenForMessages()
                    }
                } else {
                    Toast.makeText(this, "Tidak ada dokter ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal ambil data dokter", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadCurrentUserName(onComplete: () -> Unit) {
        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                currentUserName = doc.getString("name")
                    ?: auth.currentUser?.displayName
                            ?: "User"
                onComplete()
            }
            .addOnFailureListener {
                currentUserName = auth.currentUser?.displayName ?: "User"
                onComplete()
            }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAttachment.setOnClickListener {
            Toast.makeText(this, "Kirim foto hasil scan", Toast.LENGTH_SHORT).show()
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

    private fun listenForMessages() {
        db.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    chatMessages.clear()
                    for (doc in snapshot.documents) {
                        val senderId = doc.getString("senderId") ?: ""
                        val type = if (senderId == currentUserId) {
                            MessageType.SENT
                        } else {
                            MessageType.RECEIVED
                        }

                        val message = ChatMessage(
                            id = doc.id,
                            senderName = currentUserName,
                            message = doc.getString("text") ?: "",
                            timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                            type = type,
                        )
                        chatMessages.add(message)
                    }
                    chatAdapter.notifyDataSetChanged()
                    scrollToBottom()
                }
            }
    }

    private fun sendMessage(message: String) {
        val messageMap = hashMapOf(
            "text" to message,
            "senderId" to currentUserId,
            "timestamp" to Date(),
            "type" to "text",
            "isRead" to false
        )

        db.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .add(messageMap)
            .addOnSuccessListener {
                db.collection("chatRooms")
                    .document(chatRoomId)
                    .set(
                        mapOf(
                            "lastMessage" to message,
                            "lastSenderId" to currentUserId,
                            "lastTimestamp" to Date(),
                            "doctorId" to doctorId,
                            "doctorName" to doctorName,
                            "userId" to currentUserId,
                            "userName" to currentUserName
                        )
                    )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim pesan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            binding.rvChat.scrollToPosition(chatMessages.size - 1)
        }
    }
}
