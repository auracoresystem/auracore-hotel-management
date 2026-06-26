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
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _guests = MutableStateFlow<List<Guest>>(emptyList())
    val guests: StateFlow<List<Guest>> = _guests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRooms()
        loadGuests()
    }

    private fun loadRooms() {
        firestore.collection("rooms")
            .orderBy("roomNumber")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _rooms.value = snapshot.toObjects(Room::class.java)
                }
            }
    }

    private fun loadGuests() {
        firestore.collection("guests")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _guests.value = snapshot.toObjects(Guest::class.java)
                }
            }
    }

    fun addRoom(roomNumber: String) {
        viewModelScope.launch {
            val room = Room(
                id = firestore.collection("rooms").document().id,
                roomNumber = roomNumber
            )
            firestore.collection("rooms").document(room.id).set(room).await()
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
                if (!idPhotoUriString.isNullOrEmpty()) {
                    val uri = Uri.parse(idPhotoUriString)
                    val ref = storage.reference.child("guest_ids/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val guestIdGen = "G-${System.currentTimeMillis()}"
                
                val guest = Guest(
                    id = firestore.collection("guests").document().id,
                    guestId = guestIdGen,
                    name = name,
                    idPhotoUrl = photoUrl,
                    notes = notes,
                    paymentStatus = paymentStatus,
                    checkInDate = System.currentTimeMillis(),
                    expectedCheckOutDate = expectedCheckOutDate,
                    history = listOf("Checked into room")
                )

                firestore.collection("guests").document(guest.id).set(guest).await()
                firestore.collection("rooms").document(roomId).update(
                    mapOf(
                        "status" to "Occupied",
                        "currentGuestId" to guest.id
                    )
                ).await()

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
                firestore.collection("guests").document(guestId).update(
                    mapOf(
                        "actualCheckOutDate" to System.currentTimeMillis()
                    )
                ).await()

                firestore.collection("rooms").document(roomId).update(
                    mapOf(
                        "status" to "Available",
                        "currentGuestId" to null
                    )
                ).await()
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
                firestore.collection("rooms").document(oldRoomId).update(
                    mapOf(
                        "status" to "Available",
                        "currentGuestId" to null
                    )
                ).await()
                
                firestore.collection("rooms").document(newRoomId).update(
                    mapOf(
                        "status" to "Occupied",
                        "currentGuestId" to guestId
                    )
                ).await()
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
