package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "rooms")
data class RoomStatus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomNumber: String,
    val isCleaned: Boolean = true,
    val guestName: String? = null // Null means empty
)

@Entity(tableName = "repairs")
data class RepairRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val location: String,
    val isFixed: Boolean = false
)

@Entity(tableName = "staff")
data class StaffMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val isOnDuty: Boolean = true
)

// --- DAOs ---

@Dao
interface HotelDao {
    // Rooms
    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun getAllRooms(): Flow<List<RoomStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomStatus)

    @Update
    suspend fun updateRoom(room: RoomStatus)
    
    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteRoom(id: Int)

    // Repairs
    @Query("SELECT * FROM repairs ORDER BY id DESC")
    fun getAllRepairs(): Flow<List<RepairRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: RepairRequest)

    @Update
    suspend fun updateRepair(repair: RepairRequest)

    // Staff
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember)

    @Update
    suspend fun updateStaff(staff: StaffMember)
}
