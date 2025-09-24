package com.example.sadamoo.users.models

import java.util.Date

data class HistoryItem(
    val id: String,
    val type: HistoryType, // SCAN atau CONSULTATION
    val title: String,
    val subtitle: String,
    val date: Date,
    val status: String, // "Selesai", "Berlangsung", "Menunggu"
    val imageRes: Int? = null,
    val cattleType: String? = null,
    val diseaseDetected: String? = null,
    val doctorName: String? = null,
    val consultationDuration: String? = null,
    val severity: String? = null
)

enum class HistoryType {
    SCAN, CONSULTATION
}
