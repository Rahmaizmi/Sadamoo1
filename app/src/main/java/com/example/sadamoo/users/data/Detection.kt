package com.example.sadamoo.users.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "detection")
@Parcelize
data class Detection(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "imageUri")
    var uri: String,

    @ColumnInfo(name = "_name")
    var disease_name: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "confidence")
    var confidence: Float = 0.0F,

    @ColumnInfo(name = "detected_at")
    var detectedAt: String

) : Parcelable
