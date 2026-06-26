package com.example.wastage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.data.HotelDao
import com.example.data.RepairRequest
import com.example.data.RoomStatus
import com.example.data.StaffMember

@Database(entities = [WasteRecord::class, RoomStatus::class, RepairRequest::class, StaffMember::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wasteDao(): WasteDao
    abstract fun hotelDao(): HotelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auracore_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
