package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class LaundryTicket(
    val id: String = "",
    val ticketNumber: String = "",
    val type: String = "Linen", // Linen, Uniform, Guest
    val receivedDate: Long = 0L,
    val washStatus: String = "Pending", // Pending, Washing, Ironing, Ready, Delivered
    val ironStatus: String = "Pending",
    val deliveryStatus: String = "Pending",
    val quantity: Int = 0,
    val returnedQuantity: Int = 0,
    val damagedItems: Int = 0,
    val damagePhotoUrl: String = "",
    val notes: String = "",
    val staffId: String = "",
    val staffName: String = ""
)

class LaundryViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }

    private val _tickets = MutableStateFlow<List<LaundryTicket>>(emptyList())
    val tickets: StateFlow<List<LaundryTicket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineTickets = listOf(
        LaundryTicket(id = "ticket_l_1", ticketNumber = "LND-239102", type = "Linen", receivedDate = System.currentTimeMillis() - 3600000, washStatus = "Washing", ironStatus = "Pending", deliveryStatus = "Pending", quantity = 45, staffName = "Sunita Verma"),
        LaundryTicket(id = "ticket_l_2", ticketNumber = "LND-239103", type = "Uniform", receivedDate = System.currentTimeMillis() - 7200000, washStatus = "Ready", ironStatus = "Ready", deliveryStatus = "Pending", quantity = 15, staffName = "Sunita Verma")
    )

    init {
        loadTickets()
    }

    private fun loadTickets() {
        _tickets.value = offlineTickets
        val db = firestore ?: return
        try {
            db.collection("laundry_tickets")
                .orderBy("receivedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _tickets.value = snapshot.toObjects(LaundryTicket::class.java)
                    } else {
                        _tickets.value = offlineTickets
                    }
                }
        } catch (e: Exception) {
            _tickets.value = offlineTickets
        }
    }

    fun createTicket(type: String, qty: Int, staffName: String, damagePhotoUri: String?, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                val firebaseStorage = storage
                if (damagePhotoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(damagePhotoUri)
                    val ref = firebaseStorage.reference.child("laundry/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val db = firestore
                val newId = db?.collection("laundry_tickets")?.document()?.id ?: "lnd_${System.currentTimeMillis()}"
                val ticket = LaundryTicket(
                    id = newId,
                    ticketNumber = "LND-${System.currentTimeMillis()}",
                    type = type,
                    quantity = qty,
                    staffName = staffName,
                    notes = notes,
                    damagePhotoUrl = photoUrl,
                    receivedDate = System.currentTimeMillis()
                )

                if (db != null) {
                    db.collection("laundry_tickets").document(ticket.id).set(ticket).await()
                } else {
                    val updated = _tickets.value.toMutableList()
                    updated.add(0, ticket)
                    _tickets.value = updated
                }
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTicketStatus(ticketId: String, wash: String, iron: String, delivery: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                if (db != null) {
                    db.collection("laundry_tickets").document(ticketId).update(
                        mapOf(
                            "washStatus" to wash,
                            "ironStatus" to iron,
                            "deliveryStatus" to delivery
                        )
                    ).await()
                } else {
                    val index = _tickets.value.indexOfFirst { it.id == ticketId }
                    if (index != -1) {
                        val updated = _tickets.value.toMutableList()
                        updated[index] = updated[index].copy(
                            washStatus = wash,
                            ironStatus = iron,
                            deliveryStatus = delivery
                        )
                        _tickets.value = updated
                    }
                }
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
