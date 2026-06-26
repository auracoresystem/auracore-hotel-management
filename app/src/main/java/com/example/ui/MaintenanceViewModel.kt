package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class MaintenanceTicket(
    val id: String = "",
    val ticketNumber: String = "",
    val category: String = "",
    val priority: String = "Low",
    val status: String = "Pending", // Pending, In Progress, Completed
    val assignedTechnicianId: String = "",
    val assignedTechnicianName: String = "",
    val beforePhotoUrl: String = "",
    val afterPhotoUrl: String = "",
    val repairNotes: String = "",
    val createdAt: Long = 0L,
    val completedAt: Long = 0L,
    val completedById: String = "",
    val completedByName: String = "",
    val managerApproval: Boolean = false,
    val roomNumber: String = ""
)

class MaintenanceViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _tickets = MutableStateFlow<List<MaintenanceTicket>>(emptyList())
    val tickets: StateFlow<List<MaintenanceTicket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        firestore.collection("maintenance_tickets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _tickets.value = snapshot.toObjects(MaintenanceTicket::class.java)
                }
            }
    }

    fun createTicket(
        roomNumber: String,
        category: String,
        priority: String,
        beforePhotoUri: String?,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                if (beforePhotoUri != null) {
                    val uri = Uri.parse(beforePhotoUri)
                    val ref = storage.reference.child("maintenance/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val ticketNum = "MNT-${System.currentTimeMillis()}"
                
                val ticket = MaintenanceTicket(
                    id = firestore.collection("maintenance_tickets").document().id,
                    ticketNumber = ticketNum,
                    roomNumber = roomNumber,
                    category = category,
                    priority = priority,
                    repairNotes = notes,
                    beforePhotoUrl = photoUrl,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("maintenance_tickets").document(ticket.id).set(ticket).await()
                onSuccess()
            } catch (e: Exception) {
                // error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTicket(
        ticketId: String,
        status: String,
        afterPhotoUri: String?,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, Any>(
                    "status" to status
                )
                
                if (afterPhotoUri != null) {
                    val uri = Uri.parse(afterPhotoUri)
                    val ref = storage.reference.child("maintenance/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    updates["afterPhotoUrl"] = ref.downloadUrl.await().toString()
                }
                
                if (notes.isNotEmpty()) {
                    updates["repairNotes"] = notes
                }

                if (status == "Completed") {
                    updates["completedAt"] = System.currentTimeMillis()
                    val user = auth.currentUser
                    if (user != null) {
                        val userDoc = firestore.collection("users").document(user.uid).get().await()
                        updates["completedById"] = user.uid
                        updates["completedByName"] = userDoc.getString("name") ?: "Technician"
                    }
                }

                firestore.collection("maintenance_tickets").document(ticketId).update(updates).await()
                onSuccess()
            } catch (e: Exception) {
                // error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
