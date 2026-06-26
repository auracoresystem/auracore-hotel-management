package com.example.wastage

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "waste_records")
data class WasteRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val reason: String,
    val remarks: String,
    val photoUri: String?, // Stores local URI of picked photo
    val timestamp: Long = System.currentTimeMillis(),
    val staffName: String = "Current User",
    val status: String = "Pending" // Pending, Approved, Rejected
)
