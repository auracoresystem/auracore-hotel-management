package com.example.data

import kotlinx.coroutines.flow.Flow

class HotelRepository(private val dao: HotelDao) {
    val allRooms: Flow<List<RoomStatus>> = dao.getAllRooms()
    val allRepairs: Flow<List<RepairRequest>> = dao.getAllRepairs()
    val allStaff: Flow<List<StaffMember>> = dao.getAllStaff()

    suspend fun insertRoom(room: RoomStatus) = dao.insertRoom(room)
    suspend fun updateRoom(room: RoomStatus) = dao.updateRoom(room)
    suspend fun deleteRoom(id: Int) = dao.deleteRoom(id)

    suspend fun insertRepair(repair: RepairRequest) = dao.insertRepair(repair)
    suspend fun updateRepair(repair: RepairRequest) = dao.updateRepair(repair)

    suspend fun insertStaff(staff: StaffMember) = dao.insertStaff(staff)
    suspend fun updateStaff(staff: StaffMember) = dao.updateStaff(staff)
}
