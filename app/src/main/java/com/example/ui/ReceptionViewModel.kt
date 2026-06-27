package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class Guest(
    val id: String = "",
    val guestId: String = "",
    val name: String = "",
    val idPhotoUrl: String = "",
    val notes: String = "",
    val paymentStatus: String = "Pending",
    val checkInDate: Long = 0L,
    val expectedCheckOutDate: Long = 0L,
    val actualCheckOutDate: Long = 0L,
    val history: List<String> = emptyList()
)

data class Room(
    val id: String = "",
    val roomNumber: String = "",
    val status: String = "Available", // Available, Occupied, Maintenance
    val currentGuestId: String? = null
)

class ReceptionViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _guests = MutableStateFlow<List<Guest>>(emptyList())
    val guests: StateFlow<List<Guest>> = _guests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineRooms = listOf(
        Room(id = "room_101", roomNumber = "101", status = "Available"),
        Room(id = "room_102", roomNumber = "102", status = "Occupied", currentGuestId = "guest_aarav_sharma"),
        Room(id = "room_103", roomNumber = "103", status = "Maintenance"),
        Room(id = "room_104", roomNumber = "104", status = "Available"),
        Room(id = "room_201", roomNumber = "201", status = "Available")
    )
    private val offlineGuests = listOf(
        Guest(
            id = "guest_aarav_sharma",
            guestId = "G-102",
            name = "Aarav Sharma",
            notes = "VVIP guest, prefers morning newspaper & extra bottled water.",
            paymentStatus = "Paid",
            checkInDate = System.currentTimeMillis() - 86400000,
            expectedCheckOutDate = System.currentTimeMillis() + 86400000,
            history = listOf("Checked into room 102")
        )
    )

    init {
        loadRooms()
        loadGuests()
    }

    private fun loadRooms() {
        _rooms.value = offlineRooms
        val db = firestore ?: return
        try {
            db.collection("rooms")
                .orderBy("roomNumber")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _rooms.value = snapshot.toObjects(Room::class.java)
                    } else {
                        _rooms.value = offlineRooms
                    }
                }
        } catch (e: Exception) {
            _rooms.value = offlineRooms
        }
    }

    private fun loadGuests() {
        _guests.value = offlineGuests
        val db = firestore ?: return
        try {
            db.collection("guests")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _guests.value = snapshot.toObjects(Guest::class.java)
                    } else {
                        _guests.value = offlineGuests
                    }
                }
        } catch (e: Exception) {
            _guests.value = offlineGuests
        }
    }

    fun addRoom(roomNumber: String) {
        viewModelScope.launch {
            val db = firestore
            val newId = db?.collection("rooms")?.document()?.id ?: "room_${System.currentTimeMillis()}"
            val room = Room(
                id = newId,
                roomNumber = roomNumber
            )
            if (db != null) {
                db.collection("rooms").document(room.id).set(room).await()
            } else {
                val updated = _rooms.value.toMutableList()
                updated.add(room)
                _rooms.value = updated.sortedBy { it.roomNumber }
            }
        }
    }

    fun checkInGuest(
        roomId: String,
        name: String,
        notes: String,
        paymentStatus: String,
        expectedCheckOutDate: Long,
        idPhotoUriString: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                val firebaseStorage = storage
                if (!idPhotoUriString.isNullOrEmpty() && firebaseStorage != null) {
                    val uri = Uri.parse(idPhotoUriString)
                    val ref = firebaseStorage.reference.child("guest_ids/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val guestIdGen = "G-${System.currentTimeMillis()}"
                
                val db = firestore
                val newId = db?.collection("guests")?.document()?.id ?: "guest_${System.currentTimeMillis()}"
                val guest = Guest(
                    id = newId,
                    guestId = guestIdGen,
                    name = name,
                    idPhotoUrl = photoUrl,
                    notes = notes,
                    paymentStatus = paymentStatus,
                    checkInDate = System.currentTimeMillis(),
                    expectedCheckOutDate = expectedCheckOutDate,
                    history = listOf("Checked into room")
                )

                if (db != null) {
                    db.collection("guests").document(guest.id).set(guest).await()
                    db.collection("rooms").document(roomId).update(
                        mapOf(
                            "status" to "Occupied",
                            "currentGuestId" to guest.id
                        )
                    ).await()
                } else {
                    val updatedGuests = _guests.value.toMutableList()
                    updatedGuests.add(guest)
                    _guests.value = updatedGuests

                    val updatedRooms = _rooms.value.toMutableList()
                    val index = updatedRooms.indexOfFirst { it.id == roomId }
                    if (index != -1) {
                        updatedRooms[index] = updatedRooms[index].copy(
                            status = "Occupied",
                            currentGuestId = guest.id
                        )
                        _rooms.value = updatedRooms
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Check-in failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkOutGuest(roomId: String, guestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                if (db != null) {
                    db.collection("guests").document(guestId).update(
                        mapOf(
                            "actualCheckOutDate" to System.currentTimeMillis()
                        )
                    ).await()

                    db.collection("rooms").document(roomId).update(
                        mapOf(
                            "status" to "Available",
                            "currentGuestId" to null
                        )
                    ).await()
                } else {
                    val updatedGuests = _guests.value.toMutableList()
                    val gIndex = updatedGuests.indexOfFirst { it.id == guestId }
                    if (gIndex != -1) {
                        updatedGuests[gIndex] = updatedGuests[gIndex].copy(
                            actualCheckOutDate = System.currentTimeMillis()
                        )
                        _guests.value = updatedGuests
                    }

                    val updatedRooms = _rooms.value.toMutableList()
                    val rIndex = updatedRooms.indexOfFirst { it.id == roomId }
                    if (rIndex != -1) {
                        updatedRooms[rIndex] = updatedRooms[rIndex].copy(
                            status = "Available",
                            currentGuestId = null
                        )
                        _rooms.value = updatedRooms
                    }
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun changeRoom(guestId: String, oldRoomId: String, newRoomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                if (db != null) {
                    db.collection("rooms").document(oldRoomId).update(
                        mapOf(
                            "status" to "Available",
                            "currentGuestId" to null
                        )
                    ).await()
                    
                    db.collection("rooms").document(newRoomId).update(
                        mapOf(
                            "status" to "Occupied",
                            "currentGuestId" to guestId
                        )
                    ).await()
                } else {
                    val updatedRooms = _rooms.value.toMutableList()
                    val oldIndex = updatedRooms.indexOfFirst { it.id == oldRoomId }
                    if (oldIndex != -1) {
                        updatedRooms[oldIndex] = updatedRooms[oldIndex].copy(
                            status = "Available",
                            currentGuestId = null
                        )
                    }
                    val newIndex = updatedRooms.indexOfFirst { it.id == newRoomId }
                    if (newIndex != -1) {
                        updatedRooms[newIndex] = updatedRooms[newIndex].copy(
                            status = "Occupied",
                            currentGuestId = guestId
                        )
                    }
                    _rooms.value = updatedRooms
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
