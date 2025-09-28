package com.example.sadamoo.users

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.ActivityDoctorChatBinding
import com.example.sadamoo.users.adapters.MessageAdapter
import com.example.sadamoo.users.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class DoctorChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private var chatRoomId: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRoomId = intent.getStringExtra("chatRoomId")
        userName = intent.getStringExtra("userName")

        binding.tvChatTitle.text = "$userName"

        adapter = MessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        loadMessages()

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        db.collection("chatRooms")
            .document(chatRoomId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    messages.clear()
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) messages.add(msg.copy(id = doc.id))
                    }
                    adapter.notifyDataSetChanged()
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        val currentUser = auth.currentUser ?: return

        if (text.isEmpty()) return

        val message = hashMapOf(
            "senderId" to currentUser.uid,
            "senderRole" to "doctor",
            "text" to text,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        val chatRoomRef = db.collection("chatRooms").document(chatRoomId!!)

        // Tambahkan pesan ke subkoleksi
        chatRoomRef.collection("messages")
            .add(message)
            .addOnSuccessListener {
                binding.etMessage.text.clear()

                // 🔥 Update lastMessage & lastTimestamp di dokumen chatRooms
                val updates = mapOf(
                    "lastMessage" to text,
                    "lastSenderId" to currentUser.uid,
                    "lastTimestamp" to com.google.firebase.Timestamp.now()
                )
                chatRoomRef.update(updates)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
            }
    }

}
