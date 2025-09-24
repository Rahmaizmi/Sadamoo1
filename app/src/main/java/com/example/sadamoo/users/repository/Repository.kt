package com.example.sadamoo.users.repository

import androidx.lifecycle.LiveData
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val detectionRoomDatabase: DetectionRoomDatabase) {
    private val detectionDao = detectionRoomDatabase.detectionDao()

    fun getAllDetection(): LiveData<List<Detection>> {
        return detectionDao.getAllDetections()
    }

    suspend fun insertDetection(detection: Detection) {
        withContext(Dispatchers.IO) {
            detectionDao.insert(detection)
        }
    }
}