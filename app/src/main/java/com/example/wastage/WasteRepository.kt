package com.example.wastage

import kotlinx.coroutines.flow.Flow

class WasteRepository(private val wasteDao: WasteDao) {
    val allWasteRecords: Flow<List<WasteRecord>> = wasteDao.getAllWasteRecords()

    suspend fun insert(record: WasteRecord) {
        wasteDao.insertWasteRecord(record)
    }

    suspend fun updateStatus(id: Int, status: String) {
        wasteDao.updateStatus(id, status)
    }
}
