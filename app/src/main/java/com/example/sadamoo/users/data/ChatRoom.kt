package com.example.sadamoo.users.data

import com.google.firebase.Timestamp

data class ChatRoom(
    val id: String = "",
    val doctorId: String = "",
    val userId: String = "",
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val lastTimestamp: Timestamp? = null,
    val userName: String = ""
)
