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
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _tickets = MutableStateFlow<List<MaintenanceTicket>>(emptyList())
    val tickets: StateFlow<List<MaintenanceTicket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineTickets = listOf(
        MaintenanceTicket(
            id = "ticket_m_1",
            ticketNumber = "MNT-103",
            roomNumber = "103",
            category = "Electrical / AC",
            priority = "High",
            status = "Pending",
            repairNotes = "AC compressor is making a loud noise and cooling is weak.",
            createdAt = System.currentTimeMillis() - 7200000
        )
    )

    init {
        loadTickets()
    }

    private fun loadTickets() {
        _tickets.value = offlineTickets
        val db = firestore ?: return
        try {
            db.collection("maintenance_tickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _tickets.value = snapshot.toObjects(MaintenanceTicket::class.java)
                    } else {
                        _tickets.value = offlineTickets
                    }
                }
        } catch (e: Exception) {
            _tickets.value = offlineTickets
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
                val firebaseStorage = storage
                if (beforePhotoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(beforePhotoUri)
                    val ref = firebaseStorage.reference.child("maintenance/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val ticketNum = "MNT-${System.currentTimeMillis()}"
                
                val db = firestore
                val newId = db?.collection("maintenance_tickets")?.document()?.id ?: "ticket_${System.currentTimeMillis()}"
                val ticket = MaintenanceTicket(
                    id = newId,
                    ticketNumber = ticketNum,
                    roomNumber = roomNumber,
                    category = category,
                    priority = priority,
                    repairNotes = notes,
                    beforePhotoUrl = photoUrl,
                    createdAt = System.currentTimeMillis()
                )

                if (db != null) {
                    db.collection("maintenance_tickets").document(ticket.id).set(ticket).await()
                } else {
                    val updated = _tickets.value.toMutableList()
                    updated.add(0, ticket)
                    _tickets.value = updated
                }
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
                val firebaseStorage = storage
                
                if (afterPhotoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(afterPhotoUri)
                    val ref = firebaseStorage.reference.child("maintenance/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    updates["afterPhotoUrl"] = ref.downloadUrl.await().toString()
                }
                
                if (notes.isNotEmpty()) {
                    updates["repairNotes"] = notes
                }

                val db = firestore
                if (status == "Completed") {
                    updates["completedAt"] = System.currentTimeMillis()
                    val firebaseAuth = auth
                    val user = firebaseAuth?.currentUser
                    if (user != null && db != null) {
                        val userDoc = db.collection("users").document(user.uid).get().await()
                        updates["completedById"] = user.uid
                        updates["completedByName"] = userDoc.getString("name") ?: "Technician"
                    }
                }

                if (db != null) {
                    db.collection("maintenance_tickets").document(ticketId).update(updates).await()
                } else {
                    val index = _tickets.value.indexOfFirst { it.id == ticketId }
                    if (index != -1) {
                        val updated = _tickets.value.toMutableList()
                        var ticket = updated[index]
                        if (updates.containsKey("afterPhotoUrl")) ticket = ticket.copy(afterPhotoUrl = updates["afterPhotoUrl"] as String)
                        if (updates.containsKey("repairNotes")) ticket = ticket.copy(repairNotes = notes)
                        if (updates.containsKey("completedAt")) ticket = ticket.copy(completedAt = updates["completedAt"] as Long, completedByName = "Local Staff")
                        ticket = ticket.copy(status = status)
                        updated[index] = ticket
                        _tickets.value = updated
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                // error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
