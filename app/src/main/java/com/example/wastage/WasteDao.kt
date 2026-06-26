package com.example.wastage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    @Query("SELECT * FROM waste_records ORDER BY timestamp DESC")
    fun getAllWasteRecords(): Flow<List<WasteRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteRecord(record: WasteRecord)

    @Query("UPDATE waste_records SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}
