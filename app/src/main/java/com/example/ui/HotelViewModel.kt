package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HotelViewModel(private val repository: HotelRepository) : ViewModel() {

    val rooms: StateFlow<List<RoomStatus>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repairs: StateFlow<List<RepairRequest>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staff: StateFlow<List<StaffMember>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -- Rooms --
    fun addRoom(roomNumber: String) {
        viewModelScope.launch {
            repository.insertRoom(RoomStatus(roomNumber = roomNumber))
        }
    }

    fun updateRoomStatus(room: RoomStatus, isCleaned: Boolean) {
        viewModelScope.launch {
            repository.updateRoom(room.copy(isCleaned = isCleaned))
        }
    }

    fun checkInGuest(room: RoomStatus, guestName: String) {
        viewModelScope.launch {
            repository.updateRoom(room.copy(guestName = guestName, isCleaned = false)) // Need to clean after checkin usually, or maybe they just checked in
        }
    }

    fun checkOutGuest(room: RoomStatus) {
        viewModelScope.launch {
            repository.updateRoom(room.copy(guestName = null, isCleaned = false))
        }
    }

    // -- Repairs --
    fun addRepair(description: String, location: String) {
        viewModelScope.launch {
            repository.insertRepair(RepairRequest(description = description, location = location))
        }
    }

    fun toggleRepairStatus(repair: RepairRequest) {
        viewModelScope.launch {
            repository.updateRepair(repair.copy(isFixed = !repair.isFixed))
        }
    }

    // -- Staff --
    fun addStaff(name: String, role: String) {
        viewModelScope.launch {
            repository.insertStaff(StaffMember(name = name, role = role))
        }
    }

    fun toggleStaffDuty(staffMember: StaffMember) {
        viewModelScope.launch {
            repository.updateStaff(staffMember.copy(isOnDuty = !staffMember.isOnDuty))
        }
    }
}
